package uk.ac.ebi.mydas.search;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import uk.ac.ebi.mydas.controller.DasFeatureRequestFilter;
import uk.ac.ebi.mydas.controller.SegmentQuery;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.SearcherException;
import uk.ac.ebi.mydas.extendedmodel.DasUnknownFeatureSegment;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasFeature;
import uk.ac.ebi.mydas.model.DasFeatureOrientation;
import uk.ac.ebi.mydas.model.DasMethod;
import uk.ac.ebi.mydas.model.DasPhase;
import uk.ac.ebi.mydas.model.DasTarget;
import uk.ac.ebi.mydas.model.DasType;


public class Searcher {
	private static final Logger logger = Logger.getLogger(Searcher.class);
	private String dirPath, dataSourceName;
	private DasFeatureRequestFilter filter=null;
	
	public Searcher(String dirPath, String dataSourceName){
		this.dirPath = dirPath;
		this.dataSourceName = dataSourceName;
	}
	
	public Collection<DasAnnotatedSegment> search(DasFeatureRequestFilter filter) throws SearcherException{
		this.filter=filter;
		String query = this.getMergedQuery(filter);
		if (filter.getRows()==null)
			return this.search(query, null,null);
		return this.search(query, filter.getRows().getFrom(), filter.getRows().getTo());
	}
	public Collection<DasAnnotatedSegment> search(String query, Integer from, Integer to) throws SearcherException{
		try {
			query = URLDecoder.decode(query,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new SearcherException("Error trying to URLdecode the query",e);
		}
		
		StandardAnalyzer analyzer = new StandardAnalyzer();

		Directory fsDir=null;
		try {
                    fsDir = FSDirectory.open(new File(dirPath+"/"+dataSourceName).toPath());
		} catch (IOException e) {
			throw new SearcherException("Error trying to open the index file",e);
		}
		IndexSearcher searcher=null;
                IndexReader dReader = null;
		try {
                        dReader = DirectoryReader.open(fsDir);
                        searcher = new IndexSearcher(dReader);
		} catch (CorruptIndexException e) {
			throw new SearcherException("The index file is corrupt",e);
		} catch (IOException e) {
			throw new SearcherException("Error trying to open the index file.",e);
		}


		Query q=null;
		try {
			q = new QueryParser("title", analyzer).parse(query);
		} catch (ParseException e) {
			throw new SearcherException("Error parsing the query.",e);
		}

		int hitsPerPage = 100000;
		MyDasCollector collector = MyDasCollector.create(hitsPerPage, true,searcher);
		try {
			searcher.search(q, collector);
		} catch (IOException e) {
			throw new SearcherException("Error in I/O operations while searching.",e);
		}
		ScoreDoc[] hits;
//		if (from==null)
			hits= collector.topDocs().scoreDocs;
//		else
//			hits= collector.topDocs(from,to-from).scoreDocs;
		filter.setPaginated(true);
		filter.setTotalFeatures(collector.getTotalHits());
		Collection<DasAnnotatedSegment> segments= new ArrayList<DasAnnotatedSegment>();
		if (hits.length==0)
			try {
				segments.add(new DasUnknownFeatureSegment(query));
			} catch (DataSourceException e1) {
				throw new SearcherException("The resultset was empty but was impossible to generete the XML",e1);
			}
			

		if ((to==null)||(to>hits.length)) 
			to=hits.length;
		if ((from==null)||(from<1))  
			from=1;
		for(int i=from;i<=to;++i) {
			int docId = hits[i-1].doc;
			try {
				DasAnnotatedSegment segment = getSegmentFromDoc(searcher.doc(docId));
				segment.setTotalFeatures(collector.getSizePerSegment(searcher.doc(docId).get("segmentId")));
				addSegment2Collection(segments,segment);
			} catch (CorruptIndexException e) {
				throw new SearcherException("Error recovering one of the result docs.",e);
			} catch (IOException e) {
				throw new SearcherException("I/O Error while recovering one of the result docs.",e);
			}
		}

		try {
			dReader.close();
		} catch (IOException e) {
			throw new SearcherException("Error closing the searcher.",e);
		}

		return segments;
	}

	private void addSegment2Collection(Collection<DasAnnotatedSegment> segments, DasAnnotatedSegment segment) {
		for (DasAnnotatedSegment segmentAdded: segments){
			if (segmentAdded.getSegmentId().equals(segment.getSegmentId())){
				segmentAdded.getFeatures().addAll(segment.getFeatures());
				return;
			}
		}
		segments.add(segment);
	}

	private DasAnnotatedSegment getSegmentFromDoc(Document document) throws SearcherException {
		
			try {
				if (document.get("segmentStart")!=null && document.get("segmentStop")!=null)
					return new DasAnnotatedSegment(document.get("segmentId"), new Integer(document.get("segmentStart")), new Integer(document.get("segmentStop")), document.get("segmentVersion"), document.get("segmentLabel"), getFeaturesFromDoc(document));
				else
					return new DasAnnotatedSegment(document.get("segmentId"), null,null, document.get("segmentVersion"), document.get("segmentLabel"), getFeaturesFromDoc(document));
			} catch (NumberFormatException e) {
				throw new SearcherException("Number Format Error creating the segment from the lucene document.",e);
			} catch (DataSourceException e) {
				throw new SearcherException("Error creating the segment from the lucene document.",e);
			}
	}

	private Collection<DasFeature> getFeaturesFromDoc(Document document) throws SearcherException {
		int start=0,stop=0;
		Double score=null;
		if (document.get("start")!=null) 	start = new Integer(document.get("start"));
		if (document.get("stop")!=null) 	stop = new Integer(document.get("stop"));
		if (document.get("score")!=null) 	score = new Double(document.get("score"));
		DasFeatureOrientation orientation =null;
		if (document.get("orientation")!=null){
			String aux =document.get("orientation");
			if (aux.equals("+")) 	orientation=DasFeatureOrientation.ORIENTATION_SENSE_STRAND;
			if (aux.equals("-")) 	orientation=DasFeatureOrientation.ORIENTATION_ANTISENSE_STRAND;
			if (aux.equals("0")) 	orientation=DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE;
		}
		DasPhase phase =null;
		if (document.get("phase")!=null){
			String aux =document.get("phase");
			if (aux.equals("0")) 	phase=DasPhase.PHASE_READING_FRAME_0;
			if (aux.equals("1")) 	phase=DasPhase.PHASE_READING_FRAME_1;
			if (aux.equals("2")) 	phase=DasPhase.PHASE_READING_FRAME_2;
			if (aux.equals("-")) 	phase=DasPhase.PHASE_NOT_APPLICABLE;
		}
		DasFeature feature=null;
		try {
			feature = new DasFeature(	document.get("featureId"), 
												document.get("featureLabel"), 
												getTypeFromDoc(document), 
												getMethodFromDoc(document), 
												start, 
												stop, 
												score, 
												orientation, 
												phase, 
												getNotesFromDoc(document), 
												getLinksFromDoc(document), 
												getTargetsFromDoc(document), 
												getParentsFromDoc(document), 
												getPartsFromDoc(document));
		} catch (DataSourceException e) {
			throw new SearcherException("Error creating the feature from the lucene document.",e);
		}
		Collection<DasFeature> features= new ArrayList<DasFeature>();
		features.add(feature);
		return features;
	}

	private Collection<DasTarget> getTargetsFromDoc(Document document) throws SearcherException {
		if (document.get("targets")!=null){
			Collection<DasTarget> targetsF= new ArrayList<DasTarget>();
			String[] targets=document.get("targets").split(" ==TARGET== ");
			for(String target:targets){
				String[] targetIn=target.split(" _-_ ");
				
				try {
					if (targetIn.length==3)
						targetsF.add(new DasTarget(targetIn[0], new Integer(targetIn[1]), new Integer(targetIn[2]), null));
					else if (targetIn.length==4)
						targetsF.add(new DasTarget(targetIn[0], new Integer(targetIn[1]), new Integer(targetIn[2]), targetIn[3]));
				} catch (NumberFormatException e) {
					throw new SearcherException("Number Format Error creating the target from the lucene document.",e);
				} catch (DataSourceException e) {
					throw new SearcherException("Error creating the target from the lucene document.",e);
				}
			}
			return targetsF;
		}

		return null;
	}

	private Collection<String> getNotesFromDoc(Document document) {
		if (document.get("notes")!=null){
			Collection<String> notesF= new ArrayList<String>();
			String[] notes=document.get("notes").split(" ==NOTE== ");
			for(String note:notes)
				notesF.add(note);
			return notesF;
		}
		return null;
	}

	private DasMethod getMethodFromDoc(Document document) {
		try {
			return new DasMethod(document.get("methodId"), document.get("methodLabel"), document.get("methodCvId"));
		} catch (DataSourceException e) {
			return null;
		}
	}

	private DasType getTypeFromDoc(Document document) {
		return new DasType(document.get("typeId"), document.get("typeCategory"), document.get("typeCvId"), document.get("typeLabel"));
	}

	private Collection<String> getParentsFromDoc(Document document) {
		if (document.get("parents")!=null){
			Collection<String> parentsF= new ArrayList<String>();
			String[] parents=document.get("parents").split(" ==PARENT== ");
			for(String parent:parents)
				parentsF.add(parent);
			return parentsF;
		}
		return null;
	}

	private Map<URL, String> getLinksFromDoc(Document document) {
		if (document.get("links")!=null){
			Map<URL, String> linksF= new HashMap<URL, String>();
			String[] links=document.get("links").split(" ==LINK== ");
			for(String link:links){
				String[] linkIn=link.split(" _-_ ");
				if (linkIn.length==2)
					try {
						linksF.put(new URL(linkIn[1]), linkIn[0]);
					} catch (MalformedURLException e) {
						logger.error("The url "+linkIn[0]+" is malformed");
					}
			}
			return linksF;
		}

		return null;
	}

	private Collection<String> getPartsFromDoc(Document document) {
		if (document.get("parts")!=null){
			Collection<String> partsF= new ArrayList<String>();
			String[] parts=document.get("parts").split(" ==PART== ");
			for(String part:parts)
				partsF.add(part);
			return partsF;
		}
		return null;
	}
	
	public String getMergedQuery(DasFeatureRequestFilter filter){
		String query=filter.getAdvanceQuery();
		
		//getting the query for all the segments
		String querySegment="";
		String connector="";
		if (filter.getRequestedSegments()!=null){
			for (SegmentQuery segmentQuery:filter.getRequestedSegments()){
				querySegment = connector + "(segmentId:"+segmentQuery.getSegmentId();
				if (segmentQuery.getStartCoordinate()!=null && segmentQuery.getStopCoordinate()!=null){
					querySegment +=" AND segmentStart:["+segmentQuery.getStartCoordinate()+" TO *]";
					querySegment +=" AND segmentStop:[* TO "+segmentQuery.getStopCoordinate()+"]";
				}
				querySegment +=")";
				connector=" OR ";
			}
			query=mergeAND(query,querySegment);
		}
		
		String queryFeatureIds="";
		connector="";
		if (filter.getFeatureIds()!=null){
			for (String featureId:filter.getFeatureIds()){
				queryFeatureIds += connector + "featureId:"+featureId;
				connector=" OR ";
			}
			query=mergeAND(query,queryFeatureIds);
		}
		
		String queryCategories="";
		connector="";
		if (filter.getCategoryIds()!=null){
			for (String category:filter.getCategoryIds()){
				queryCategories += connector + "typeCategory:"+category;
				connector=" OR ";
			}
			query=mergeAND(query,queryCategories);
		}
		
		String queryTypes="";
		connector="";
		if (filter.getTypeIds()!=null){
			for (String typeId:filter.getTypeIds()){
				queryTypes += connector + "typeId:"+typeId;
				connector=" OR ";
			}
			query=mergeAND(query,queryTypes);
		}
		return query;
	}
	private String mergeAND(String q1,String q2){
		if (q1==null || q1.trim().equals("")) return q2;
		if (q2==null || q2.trim().equals("")) return q1;
		return "("+ q1 + ") AND (" + q2 +")";
	}
}
