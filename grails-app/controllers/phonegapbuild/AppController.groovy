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
