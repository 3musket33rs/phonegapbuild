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
