/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package org.wso2.carbon.appfactory.application.mgt.type;

import org.apache.axiom.om.OMElement;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.model.Model;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeManager;
import org.wso2.carbon.appfactory.utilities.project.ProjectUtils;
import org.wso2.carbon.appfactory.utilities.version.AppVersionStrategyExecutor;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Data service application type processor
 */
public class DataServiceApplicationTypeProcessor extends AbstractFreeStyleApplicationTypeProcessor {

	private static final Log log = LogFactory.getLog(DataServiceApplicationTypeProcessor.class);

	@Override
    public void doVersion(String applicationId, String targetVersion, String currentVersion, String workingDirectory)
			throws AppFactoryException {
        AppVersionStrategyExecutor.doVersionForDBS(targetVersion, new File(workingDirectory));
    }

    @Override
    public void generateApplicationSkeleton(String applicationId, String workingDirectory) throws AppFactoryException {
        ProjectUtils.generateProjectArchetype(applicationId, workingDirectory,
                ProjectUtils.getArchetypeRequest(applicationId,
                        getProperty(MAVEN_ARCHETYPE_REQUEST)));
        File pomFile = new File(workingDirectory + File.separator + AppFactoryConstants.DEFAULT_POM_FILE);
        boolean result = FileUtils.deleteQuietly(pomFile);
        if (!result){
            log.warn("Error while deleting pom.xml for application id : " + applicationId);
        }
	    configureFinalName(workingDirectory, applicationId,
	                       AppFactoryUtil.getAppfactoryConfiguration().
			                       getFirstProperty(AppFactoryConstants.PROPERTY_ARTIFACT_VERSION_NAME));
    }

	/**
	 * Generates and returns the deployment url of the application
	 *
	 * @param tenantDomain domain of the current tenant
	 * @param applicationId id of the application
	 * @param applicationVersion version of the application
	 * @param stage deployment stage of the application
	 * @return deployment url
	 * @throws AppFactoryException
	 */
	public String getDeployedURL(String tenantDomain, String applicationId,
	                             String applicationVersion, String stage) throws AppFactoryException{
		String url = (String) this.properties.get(LAUNCH_URL_PATTERN);

		String artifactTrunkVersionName = AppFactoryUtil.getAppfactoryConfiguration().
				getFirstProperty(AppFactoryConstants.TRUNK_SERVICES_ARTIFACT_VERSION_NAME);
		String sourceTrunkVersionName = AppFactoryUtil.getAppfactoryConfiguration().
				getFirstProperty(AppFactoryConstants.TRUNK_SERVICES_SOURCE_VERSION_NAME);
		if(applicationVersion.equalsIgnoreCase(sourceTrunkVersionName)) {
			applicationVersion = artifactTrunkVersionName;
		}

		String urlStageValue = (String) this.properties.get(stage + PARAM_APP_STAGE_NAME_SUFFIX);

		if(urlStageValue == null){
			urlStageValue = "";
		}

		url = url.replace(PARAM_TENANT_DOMAIN, tenantDomain).replace(PARAM_APP_ID, applicationId)
		         .replace(PARAM_APP_VERSION, applicationVersion).replace(PARAM_APP_STAGE, urlStageValue);
		return url;
	}



	private void configureFinalName(String path, String appId, String version) {
		File artifactDir = new File(path);
		Model model;
		try {
			String[] fileExtension = {"dbs"};
			List<File> fileList = (List<File>) org.apache.commons.io.FileUtils.listFiles(artifactDir,
			                                                                             fileExtension, true);

			for (File file : fileList) {
				File renamedFile = new File(file.getPath().substring(0, file.getPath().
						lastIndexOf(File.separator) + 1) + appId + "-default-" + version + ".dbs");
				file.renameTo(renamedFile);
			}
		} catch (Exception e) {
			//TODO
			e.printStackTrace();
		}
	}
}
