/* Copyright 2012 the original author or authors. *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 *
 * @author <a href='mailto:th33musk3t33rs@gmail.com'>3.musket33rs</a>
 *
 * @since 0.1
 */
 
 package phonegapbuild

import org.codehaus.groovy.grails.commons.GrailsApplication;
import grails.converters.*
import grails.plugins.rest.client.CustomRestBuilder
import org.codehaus.groovy.grails.commons.ConfigurationHolder
class PhonegapBuildService {
    GrailsApplication grailsApplication

    def zip() {
        new AntBuilder().zip(
                destfile: "client.zip",
                basedir: "web-app"
        )
    }

    def push() {
        zip()
        def rest = new CustomRestBuilder()
        def appName = grailsApplication.metadata['app.name']
        String userName = ConfigurationHolder.config.phonegapbuild?.username
        String password = ConfigurationHolder.config.phonegapbuild?.password
        String phonegapVersion =  ConfigurationHolder.config.phonegapbuild?.phonegapversion
        def resp = rest.post("https://build.phonegap.com/api/v1/apps") {
            auth userName, password
            contentType "multipart/form-data"
            file = new File("client.zip")


            data = '{"title":"' + appName + '","package":"' + appName + '","version":"0.1.0","create_method":"file","phonegap_version":"' + phonegapVersion+'" }'
        }
        def json = JSON.parse(resp.text)

        json.id
    }

    def getAppStatus(Integer appId) {
        String userName = ConfigurationHolder.config.phonegapbuild?.username
        String password = ConfigurationHolder.config.phonegapbuild?.password

        def rest = new CustomRestBuilder()

        def resp = rest.get("https://build.phonegap.com/api/v1/apps/" + appId) {
            auth userName, password
        }
        def json = JSON.parse(resp.text)


        json
    }
}
