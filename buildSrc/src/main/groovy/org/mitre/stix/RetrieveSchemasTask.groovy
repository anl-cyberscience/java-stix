/**
 * Copyright (c) 2015, The MITRE Corporation. All rights reserved.
 * See LICENSE for complete terms.
 */
package org.mitre.stix

import org.mitre.Checksum

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.GradleException

import groovy.io.FileType

import org.mitre.stix.Checksum

import org.apache.tools.ant.taskdefs.condition.Os

import groovy.xml.XmlUtil
import groovy.xml.StreamingMarkupBuilder
import groovy.util.slurpersupport.GPathResult

/**
 * Gradle Task used to attempt to automatically retrieve the schemas
 * 
 * @author nemonik (Michael Joseph Walsh <github.com@nemonik.com>)
 *
 */
class RetrieveSchemasTask extends DefaultTask {

	@Input String schemaVersion
	
	RetrieveSchemasTask() {
		description "Automatically attempt to retrieve the schemas."
	}
	
	def patch() {
		def fileToBePatched = project.file("src/main/resources/schemas/v${schemaVersion}/cybox/objects/Archive_File_Object.xsd")
		
		if (new Checksum().calc(fileToBePatched) != "e986dddfa05a2404c155b7c2b93603e4af31b4e9") {
			
			println("    Patching ${fileToBePatched}")
			
			ant.patch(patchfile: "cybox_object_archive_file_object.patch", originalfile: fileToBePatched)
		} else {
			println("    ${fileToBePatched} already patched.")
		}
	}
	
	def pull(file) {
	
		def command = null

		if (Os.isFamily(Os.FAMILY_WINDOWS)) {
			command = "cmd /c .\${file}.bat"
		} else {
			command = "sh ./${file}.sh $schemaVersion"
		}

		def proc = command.execute(null, project.rootDir)
		proc.waitFor()

		println("${proc.in.text}")
	}
	
	def processFileInplace(file, Closure processText) {
		def text = file.text
		file.write(processText(text))
	}

	
	def String serializeXml(GPathResult xml){
		// how annoying.  the default serializer just ignores root level namespaces! KLS
		XmlUtil.serialize(new StreamingMarkupBuilder().bind {
			mkp.yield xml
		  } )
	}

	@TaskAction
	def retrieve() {
		
		if (project.fileTree("src/main/resources/schemas/v${schemaVersion}").isEmpty() || project.fileTree("src/main/resources/schemas/v${schemaVersion}/cybox").isEmpty()) {
			pull("retrieve_schemas")
		} else {
			println("    Schemas are present. Retrieval is not needed.")
		}
		
		// retreive marking files if directory not present
		def markingDir = new File(project.projectDir,"src/main/resources/schemas/v${schemaVersion}/marking_extensions")
		if (!markingDir.exists()) {
			// this worked perfectly, however, the namespaces are wrong for creating JAXB classes.
			//  need to add the file substitutions after the pull call
			pull("retrieveMarkingSchemas")
			
			def ais = new File(markingDir,'AIS_Bundle_Marking_1.1.1_v1.0.xsd')
			processFileInplace(ais) { text ->
				def schemafile = new XmlSlurper(false,false).parseText(text)
				//schemafile.declareNamespace(xs: 'http://www.w3.org/2001/XMLSchema',
				//			AISMarking: 'http://www.us-cert.gov/STIXMarkingStructure#AISConsentMarking-2',
				//			marking: 'http://data-marking.mitre.org/Marking-1',
				//			stixCommon:'http://stix.mitre.org/common-1')
				schemafile.'xs:import'[0].@schemaLocation = '../data_marking.xsd'
				schemafile.'xs:import'[1].@schemaLocation = '../stix_common.xsd'
				serializeXml(schemafile)
		   }
		}
		
		patch()
	}
}