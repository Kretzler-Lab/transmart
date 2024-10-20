/*************************************************************************
 * tranSMART - translational medicine data mart
 *
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 *
 * This product includes software developed at Janssen Research & Development, LLC.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 ***************************************************************** */

package com.recomdata.transmart.util

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.commons.io.IOUtils

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Slf4j('logger')
@CompileStatic
class ZipService {

    boolean transactional = false

    private static final int BUFFER_SIZE = 250 * 1024

    /**
     * Zip a given folder.
     */
    static String zipFolder(String srcFolder, String destZipFile) {

        File zipFile = new File(destZipFile)
	if (zipFile.exists() && zipFile.isFile() && zipFile.delete()) {
	    zipFile = new File(destZipFile)
	}

        ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFile))

	addFolderToZip '', srcFolder, zip
	zip.flush()
	zip.close()

	zipFile.name
    }

    private static void addFileToZip(String path, String srcFile, ZipOutputStream zip) {

        File folder = new File(srcFile)
        if (folder.isDirectory()) {
            addFolderToZip path, srcFile, zip
	    return
        }

        byte[] buf = new byte[BUFFER_SIZE]
        int len
        FileInputStream inStream = new FileInputStream(srcFile)
        try {
            zip.putNextEntry(new ZipEntry(path + '/' + folder.name))
            while ((len = inStream.read(buf)) > 0) {
                zip.write buf, 0, len
            }
        }
        finally {
            IOUtils.closeQuietly inStream
        }
    }

    private static void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) {
        File folder = new File(srcFolder)

        for (String fileName in folder.list()) {
            if (!path) {
                addFileToZip folder.name, srcFolder + '/' + fileName, zip
            }
            else {
                addFileToZip path + '/' + folder.name, srcFolder + '/' + fileName, zip
            }
        }
    }

    /**
     * Bundle all files into a zip file.
     * If there are 2 files with the same name, only the first file is part of the zip.
     */
    static String bundleZipFile(String zipFileName, List<File> files) {
        File zipFile = null
        Map<String, File> filesMap = new HashMap<String, File>()

        if (!zipFileName) return null

        try {
            zipFile = new File(zipFileName)
            if (zipFile.exists() && zipFile.isFile() && zipFile.delete()) {
                zipFile = new File(zipFileName)
            }

            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile))
            zipOut.setLevel(ZipOutputStream.DEFLATED)
            byte[] buffer = new byte[BUFFER_SIZE]

            for (File file : files) {
                if (filesMap.containsKey(file.name)) {
                    continue
                }
                else if (file.exists() && file.canRead()) {
                    filesMap.put(file.name, file)
                    zipOut.putNextEntry(new ZipEntry(file.name))
                    FileInputStream fis = new FileInputStream(file)
                    int bytesRead
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        zipOut.write buffer, 0, bytesRead
                    }
                    zipOut.flush()
                    zipOut.closeEntry()
                }
            }
            zipOut.finish()
            zipOut.close()
        }
        catch (IOException e) {
            //logger.error('Error while creating Zip file')
        }

        return (null != zipFile) ? zipFile.absolutePath : null
    }
}
