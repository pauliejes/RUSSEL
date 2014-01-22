/*
Copyright 2012-2013 Eduworks Corporation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.eduworks.russel.ui.client.pagebuilder;

import com.eduworks.gwt.client.net.api.AlfrescoApi;
import com.eduworks.gwt.client.net.callback.AlfrescoCallback;
import com.eduworks.gwt.client.net.packet.AlfrescoPacket;
import com.eduworks.gwt.client.pagebuilder.PageAssembler;
import com.eduworks.russel.ui.client.epss.ProjectFileModel;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 * MetaBuilder class
 * Defines globals and methods for the metadata.
 * 
 * @author Eduworks Corporation
 */
public class MetaBuilder {
	public static final String DETAIL_SCREEN = "detail";
	public static final String EDIT_SCREEN = "edit";
	
	private String metaType;
	
	/**
	 * MetaBuilder Constructor for the class
	 * @param mType String Type of metadata display (either detail or edit screen)
	 */
	public MetaBuilder (String mType) {
		this.metaType = mType;
	}
	
	/**
	 * doColors Calls the javascript function getSecurityColor to assign coloring scheme to Classification, Level, and Distribution fields
	 * @param s String
	 * @return String
	 */
	private final native String doColors0(String s) /*-{
		return $wnd.getSecurityColor(s);
	}-*/;

	/**
	 * putObjectives Calls the javascript function listObjectives to display the objectives in the metadata screen
	 * @param s String Screen type
	 * @param id String 
	 * @return String
	 */
	private final native String putObjectives0(String s, String id) /*-{
		return $wnd.listObjectives(s, id);
	}-*/;

	/**
	 * getObjectives Calls the javascript function compressObjectives to retrieve and save the objective metadata edits
	 * @param id String
	 * @return String
	 */
	private final native String getObjectives0(String id) /*-{
		return $wnd.compressObjectives(id);
	}-*/;

	/**
	 * addProperty Extracts a specific field value from the metadata screen and prepares for save to node
	 * @param property String Name of metadata property
	 * @param elementID String id of metadata field
	 * @param ap AlfrescoPacket to be updated with new value 
	 */
	private void addProperty0(String property, String elementID, AlfrescoPacket ap) {
		String val = ((Label)PageAssembler.elementToWidget(elementID, PageAssembler.LABEL)).getText();
		if (val==null||val=="")
			val = ((TextBox)PageAssembler.elementToWidget(elementID, PageAssembler.TEXT)).getText();
		if (val==null)
			val = "Click to edit";
		
		if (val.trim().equalsIgnoreCase(".")) 
			if (property!="tags")
				ap.addKeyValue(property, "");
			else
				ap.addKeyArray(property, null);
		else if (!val.equalsIgnoreCase("Click to edit") && !val.equalsIgnoreCase("N/A"))
			if (property!="tags")
				ap.addKeyValue(property, val.replaceAll("\"", "'").replaceAll("[\r\n]", " ").trim());
			else
				ap.addKeyValue(property, val.split(","));
	}

	/**
	 * addObjectiveProperty Extracts the list of objectives from the metadata screen and prepares for save to node
	 * @param ap AlfrescoPacket to be updated with new objectives list
	 * @param elementID String id of objectives field
	 * @return AlfrescoPacket
	 */
	private AlfrescoPacket addObjectiveProperty0(AlfrescoPacket ap, String elementID) {
		String val = getObjectives0(elementID);
		
		if (val==null)
			val = "Click to edit";
		if (!val.equalsIgnoreCase("Click to edit") && !val.equalsIgnoreCase("N/A"))
			ap.addKeyValue("russel:objective", val.replaceAll("\"", "'").replaceAll("[\r\n]", " ").trim());
		
		return ap;
	}

	/**
	 * addMetadataToField Formats and places the appropriate value into the designated form field in the metadata screen indicated.
	 * @param property String Name of metadata property
	 * @param id String Name of element in node packet
	 * @param alfrescoPacket AlfrescoPacket information for the node
	 */
	public void addMetaDataToField(String property, String id, AlfrescoPacket alfrescoPacket) {
		String fieldVal = "Click to edit";
		if (property.equalsIgnoreCase("cm:title") || property.equalsIgnoreCase("cmis:versionLabel") || property.equalsIgnoreCase("cmis:contentStreamLength") ||
				property.equalsIgnoreCase("cmis:contentStreamMimeType") || property.equalsIgnoreCase("cmis:contentStreamLength") || property.equalsIgnoreCase("russel:epssStrategy"))
			fieldVal = "N/A";

		if (!alfrescoPacket.getRusselValue(property).trim().equalsIgnoreCase(""))
			fieldVal = alfrescoPacket.getRusselValue(property);

		// If no values are assigned for Classification, Level, or Distribution, set them to the public default values
		if ((property == "russel:class") && (fieldVal == "Click to edit")) 
			fieldVal = "Unclassified";
		else if ((property == "russel:level") && (fieldVal == "Click to edit")) 
			fieldVal = "None";
		else if ((property == "russel:dist") && (fieldVal == "Click to edit")) 
			fieldVal = "Statement A (Public)";
		
		if ((metaType.equals(EDIT_SCREEN)&&(property=="russel:objective")) ||
		    (metaType.equals(DETAIL_SCREEN)&&(property=="russel:objective"))) {
			putObjectives0(fieldVal, id);
		}
		else if (metaType.equals(EDIT_SCREEN)&&(property=="russel:class"||property=="russel:level"||property=="russel:dist")) {
			DOM.getElementById(id).setInnerHTML(doColors0(fieldVal));
		}
		else if (metaType.equals(DETAIL_SCREEN)&&(property == "russel:FLRtag")) {
			DOM.getElementById(id).setInnerHTML("<a href='"+fieldVal+"' target='_blank'>"+fieldVal+"</a");
		}
		else if (metaType.equals(DETAIL_SCREEN)&&(property == "russel:epssStrategy")) {
			ProjectFileModel.renderIsdUsage(fieldVal, id);
		}		
		else {
			((Label)PageAssembler.elementToWidget(id, PageAssembler.LABEL)).setText(fieldVal);
		}
	}
	
	/**
	 * addMetaDataFields Allocates all node information to the appropriate metadata screen fields
	 * @param ap AlfrescoPacket information for the node
	 */
	public void addMetaDataFields(AlfrescoPacket ap) {
		String ext = null;
		if ((ap != null)&&(!ap.toJSONString().equals("{}"))) {
			ext = ap.getFilename().substring(ap.getFilename().lastIndexOf(".")+1);
			if (metaType.equals(EDIT_SCREEN)) {
				addMetaDataToField("cm:title", "metaTitle", ap);
				addMetaDataToField("cm:description", "metaDescription", ap);
				addMetaDataToField("cmis:createdBy", "metaOwner", ap);
				addMetaDataToField("russel:publisher", "metaPublisher", ap);
				addMetaDataToField("russel:class", "metaClassification", ap);
				addMetaDataToField("russel:objective", "display-objective-list", ap);
				addMetaDataToField("russel:activity", "metaInteractivity", ap);
				addMetaDataToField("russel:env", "metaEnvironment", ap);
				addMetaDataToField("russel:coverage", "metaCoverage", ap);
				addMetaDataToField("russel:agerange", "metaSkill", ap);
				addMetaDataToField("russel:language", "metaLanguage", ap);
				addMetaDataToField("russel:duration", "metaDuration", ap);
				addMetaDataToField("russel:techreqs", "metaTechnicalRequirements", ap);
				addMetaDataToField("russel:dist", "metaDistribution", ap);
				addMetaDataToField("russel:level", "metaLevel", ap);
				addMetaDataToField("russel:partof", "metaPartOf", ap);
				addMetaDataToField("russel:requires", "metaRequires", ap);
				addMetaDataToField("cmis:contentStreamMimeType", "metaFormat", ap);
				addMetaDataToField("cmis:versionLabel", "metaVersion", ap);
				addMetaDataToField("cmis:contentStreamLength", "metaSize", ap);
			} else {
				addMetaDataToField("cm:title", "r-detailTitle", ap);
				addMetaDataToField("cm:title", "detailMetaTitle", ap);
				addMetaDataToField("cm:description", "detailMetaDescription", ap);
				addMetaDataToField("cmis:name", "detailMetaFilename", ap);
				addMetaDataToField("cmis:createdBy", "detailMetaOwner", ap);
				addMetaDataToField("russel:publisher", "detailMetaPublisher", ap);
				addMetaDataToField("russel:class", "detailMetaClassification", ap);
				addMetaDataToField("russel:objective", "detail-objective-list", ap);
				addMetaDataToField("russel:activity", "detailMetaInteractivity", ap);
				addMetaDataToField("russel:env", "detailMetaEnvironment", ap);
				addMetaDataToField("russel:coverage", "detailMetaCoverage", ap);
				addMetaDataToField("russel:agerange", "detailMetaSkill", ap);
				addMetaDataToField("russel:language", "detailMetaLanguage", ap);
				addMetaDataToField("russel:duration", "detailMetaDuration", ap);
				addMetaDataToField("russel:techreqs", "detailMetaTechnicalRequirements", ap);
				addMetaDataToField("russel:dist", "detailMetaDistribution", ap);
				addMetaDataToField("russel:level", "detailMetaLevel", ap);
				addMetaDataToField("russel:partof", "detailMetaPartOf", ap);
				addMetaDataToField("russel:requires", "detailMetaRequires", ap);
				addMetaDataToField("russel:epssStrategy", "detailEpssStrategies", ap);
				addMetaDataToField("cmis:contentStreamMimeType", "detailMetaFormat", ap);
				addMetaDataToField("cmis:versionLabel", "detailMetaVersion", ap);
				addMetaDataToField("cmis:contentStreamLength", "detailMetaSize", ap);
				if (ext.equalsIgnoreCase("rlr")) {
					addMetaDataToField("russel:FLRtag", "r-preview", ap);
				}
			}
		}
		String acc = "";
		for (int x=0;x<ap.getTags().length();x++)
			if (ap.getTags().get(x).trim()!="")
				acc += "," + ap.getTags().get(x).trim();
		if (acc!="") acc = acc.substring(1);
		else acc = "Click to edit";
		if (metaType.equals(EDIT_SCREEN))
			((Label)PageAssembler.elementToWidget("metaKeywords", PageAssembler.LABEL)).setText(acc);
		else
			((Label)PageAssembler.elementToWidget("detailMetaKeywords", PageAssembler.LABEL)).setText(acc);
	}
	
	/**
	 * saveMetadata Collects edited metadata and saves it to the node in Alfresco
	 * @param nodeId String Alfresco node id
	 * @param callback AlfrescoCallback<AlfrescoPacket>
	 */
	public void saveMetadata(String nodeId, AlfrescoCallback<AlfrescoPacket> callback) {	
		String postString = buildMetaPacket();
		if (postString!=null&&nodeId!=null)
			AlfrescoApi.setObjectMetadata(nodeId, postString, callback);
	}

	/**
	 * buildMetaPacket Creates a new packet and adds the current field values for each metadata field on the screen for the node.
	 * @return String node packet in JSON format
	 */
	public String buildMetaPacket() {
		AlfrescoPacket ap = AlfrescoPacket.makePacket();
		AlfrescoPacket container = AlfrescoPacket.makePacket();
		if (metaType.equals(DETAIL_SCREEN)) {
			addProperty0("cm:title", "detailMetaTitle", ap);
			addProperty0("cm:description", "detailMetaDescription", ap);
			addProperty0("russel:publisher", "detailMetaPublisher", ap);
			addProperty0("russel:class", "detailMetaClassification", ap);
			addObjectiveProperty0(ap, "detail-objective-list");
			addProperty0("russel:activity", "detailMetaInteractivity", ap);
			addProperty0("russel:env", "detailMetaEnvironment", ap);
			addProperty0("russel:coverage", "detailMetaCoverage", ap);
			addProperty0("russel:agerange", "detailMetaSkill", ap);
			addProperty0("russel:language", "detailMetaLanguage", ap);
			addProperty0("russel:duration", "detailMetaDuration", ap);
			addProperty0("russel:techreqs", "detailMetaTechnicalRequirements", ap);
			addProperty0("russel:dist", "detailMetaDistribution", ap);
			addProperty0("russel:level", "detailMetaLevel", ap);
			addProperty0("russel:partof", "detailMetaPartOf", ap);
			addProperty0("russel:requires", "detailMetaRequires", ap);
			addProperty0("russel:epssStrategy", "detailEpssStrategies", ap);
			addProperty0("tags", "detailMetaKeywords", container);
		} else {
			addProperty0("cm:title", "metaTitle", ap);
			addProperty0("cm:description", "metaDescription", ap);
			addProperty0("russel:publisher", "metaPublisher", ap);
			addProperty0("russel:class", "metaClassification", ap);
			addObjectiveProperty0(ap, "display-objective-list");
			addProperty0("russel:activity", "metaInteractivity", ap);
			addProperty0("russel:env", "metaEnvironment", ap);
			addProperty0("russel:coverage", "metaCoverage", ap);
			addProperty0("russel:agerange", "metaSkill", ap);
			addProperty0("russel:language", "metaLanguage", ap);
			addProperty0("russel:duration", "metaDuration", ap);
			addProperty0("russel:techreqs", "metaTechnicalRequirements", ap);
			addProperty0("russel:dist", "metaDistribution", ap);
			addProperty0("russel:level", "metaLevel", ap);
			addProperty0("russel:partof", "metaPartOf", ap);
			addProperty0("russel:requires", "metaRequires", ap);
			addProperty0("tags", "metaKeywords", container);
		}
		if (!ap.toJSONString().equals("{}"))
			container.addKeyValue("properties", ap);
		if (container.toJSONString().equals("{}"))
			return null;
		return container.toJSONString();
	}
	
	/**
	 * convertToMetaPacket Translates the information in packet to JSON format required for storage in Alfresco
	 * @param ap information on the node
	 * @return String
	 */
	public static String convertToMetaPacket(AlfrescoPacket ap) {
		AlfrescoPacket container = AlfrescoPacket.makePacket();
		if (!ap.toJSONString().equals("{}"))
			container.addKeyValue("properties", ap);
		if (container.toJSONString().equals("{}"))
			return null;
		return container.toJSONString();
	}	 
	
}