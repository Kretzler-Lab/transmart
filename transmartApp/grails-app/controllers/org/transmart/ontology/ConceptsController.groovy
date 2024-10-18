package org.transmart.ontology
import grails.converters.JSON
import org.transmart.searchapp.AuthUser
import org.transmartproject.core.exceptions.InvalidArgumentsException
//import org.transmartproject.core.ontology.BoundModifier
import org.transmartproject.core.ontology.ConceptsResource
class ConceptsController {

    ConceptsResource conceptsResourceService
    def i2b2HelperService
    def springSecurityService

    def getCategories() {
        render conceptsResourceService.allCategories as JSON
    }

    def getChildren(String concept_key) {
        def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
        def parent = conceptsResourceService.getByKey(concept_key)
        def childrenWithTokens = i2b2HelperService.getChildPathsWithTokensFromParentKey(concept_key)
        def childrenWithAuth = i2b2HelperService.getAccess(childrenWithTokens)
        def authChildren = []

        parent.children.each { child->
            if (childrenWithAuth[child.fullName] != 'Locked') {
                authChildren.add(child)
            }
        }

        render authChildren as JSON
    }

   def getResource(String concept_key) {
	    render(conceptsResourceService.getByKey(concept_key) as JSON)
    }

    def getModifierChildren(String modifier_key, String applied_path, String qualified_term_key) {
        if (!modifierKey || !appliedPath || !qualifiedTermKey) {
            throw new InvalidArgumentsException('Missing arguments')
        }

        /* TODO: method needs to be added to the interface */
 /*       if (conceptsResourceService.respondsTo('getModifier')) {
            BoundModifier modifier =
                    conceptsResourceService.getModifier(
                            modifierKey, appliedPath, qualifiedTermKey)
            render modifier.children as JSON
        } else {
            throw new OperationNotSupportedException()
        }*/
    }

}
