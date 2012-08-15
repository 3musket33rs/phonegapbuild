package phonegapbuild

import org.springframework.dao.DataIntegrityViolationException

class AppController {

    def phonegapBuildService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]



    def zip() {
        phonegapBuildService.zip()
    }

    def push() {
        println "ffsdfs" + params
        def appId =	phonegapBuildService.push()
        def statuses = phonegapBuildService.getAppStatus(appId)
        Map statusesMap = splitAnswers(statuses.status)
        Map downLoadMap = splitAnswers(statuses.download)
        render(template:"nativeTable",model:[statusesMap:statusesMap,downLoadMap:downLoadMap,appId:appId])
    }

    def getStatusApp(){
        def appId = params.id as Integer
        def statuses = phonegapBuildService.getAppStatus(appId)
        Map statusesMap = splitAnswers(statuses.status)
        Map downLoadMap = splitAnswers(statuses.download)
        render(template:"nativeTable",model:[statusesMap:statusesMap,downLoadMap:downLoadMap,appId:appId])
    }

    Map splitAnswers(def answers){
        def map = [:]
        answers.each{it->
            println it
            String item = it
            def list = item.tokenize("=")
            map[list[0]] = list[1]
        }
        map
    }
    def initBuild() {
        def appName = grailsApplication.metadata['app.name']
        render(view:"initBuild",model:[appName:appName])
    }


}
