<cm_documents>
    <div class="row" if={ selected }>
         <div class="col-md-12">
            <cm_document_form ref="doc_edit"></cm_document_form>
        </div>
    </div>
    <div class="row" if={ !selected }>
        <div class="col-md-12">
            <h2>{labels.title[app.language]} 
                <virtual each={ lang, i in app.languages}>
                    <button type="button" class="btn btn-sm { lang==selectedLanguage?'btn-primary':'btn-default' }" onclick={ selectLanguage(lang) }>{ lang }</button>
                </virtual>
                <i class="material-icons clickable" onclick={ refreshDocs() }>refresh</i>
                <i class="material-icons clickable" onclick={ editDocument('NEW', true) }>add</i>
            </h2>
        </div>
    </div>
    <div class="row" if={ !selected }>
        <div class="col-md-12">
            <form class="form-inline">
                <label class="mr-2" for="pathsDropdown">{ labels.path_status[app.language] }</label>
                <select class="select mr-2" id="pathsDropdown" onchange={ selectPath }>
                    <option each={ tmpPath, index in paths }>{ tmpPath }</option>
                </select>
                <select class="select" id="statusesDropdown" onchange={ selectStatus }>
                    <option each={ tmpStatus, index in statuses }>{ tmpStatus }</option>
                </select>
            </form>
        </div>
    </div>
    <div class="row" if={ !selected }>
        <div class="col-md-12">
            <table id="doclist" class="table table-condensed">
                <thead>
                    <tr class="d-flex">
                        <th class="col-1"></th>
                        <th class="col-3">{labels.t_name[app.language]}</th>
                        <th class="col-5">{labels.t_title[app.language]}</th>
                        <!--<th>{labels.t_status[app.language]}</th>-->
                        <th class="col-3 text-right">
                            <i class="material-icons clickable" onclick={ editDocument('NEW', true) }>add</i>
                        </th>
                    </tr>
                </thead>
                <tbody>
                    <tr class="d-flex" each={doc in documents}>
                        <td class="col-1">
                            <i class="material-icons clickable" if={ doc.type == 'FILE'}>attachment</i>
                            <i class="material-icons clickable" if={ doc.type == 'CODE'}>receipt</i>
                            <i class="material-icons clickable" if={ doc.type == 'ARTICLE'}>subject</i>
                        </td>
                        <td class="col-3">{ doc.name }</td>
                        <td class="col-5">{ decodeURIComponent(doc.title) }</td>
                        <!--<td>{ doc.status }</td>-->
                        <td class="col-3 text-right">
                            <i class="material-icons clickable" onclick={ editDocument(doc.uid, false) }>open_in_browser</i>
                            <i class="material-icons clickable" onclick={ editDocument(doc.uid, true) }>mode_edit</i>
                            <i class="material-icons clickable" if={ doc.status=='wip'} onclick={ setPublished(doc.uid, true) }>visibility</i>
                            <i class="material-icons clickable" if={ doc.status=='published'} onclick={ setPublished(doc.uid, false) }>visibility_off</i>
                            <i class="material-icons clickable" onclick={ select(doc.uid) } data-toggle="modal" data-target="#removeDialog">delete</i>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
    <div class="row" >
        <div class="col-md-12">
            <div id="removeDialog" class="modal fade">
                <div class="modal-dialog modal-sm">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                            <h4 class="modal-title">{labels.remove_title[app.language]}</h4>
                        </div>
                        <div class="modal-body">
                            <p>{labels.remove_question[app.language]}</p>
                            <p class="text-warning"><small>{labels.remove_info[app.language]}</small></p>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-default" data-dismiss="modal" onclick={ select('') }>{labels.cancel[app.language]}</button>
                            <button type="button" class="btn btn-primary" data-dismiss="modal" onclick={ removeDocument() }>{labels.remove[app.language]}</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <script charset="UTF-8">
        var self = this;
        self.listener = riot.observable();
        self.path = '/'
        self.status = 'wip'
        self.paths = ['/']
        self.statuses = ['wip', 'published']
        self.documents = []
        self.selected = ''
        self.selectedLanguage = 'EN'
        self.removing = ''

        //globalEvents.on('pageselected:documents', function (eventName) {
        this.on('mount', function(){
            self.selected = ''
            self.selectedLanguage = app.language
            app.log('PAGE DOCUMENTS')
            app.log(self.statuses)
            app.log(self.paths)
            readPaths()
        });
        
        this.on('unmount',function(){
            Object.keys(self.refs).forEach(function(key) {
                self.refs[key].unmount()
            });
            self.refs=[]
        })
        
        self.listener.on('*', function (eventName) {
            app.log('LISTENER: ' + eventName)
            if(!eventName) return;
            if(eventName.startsWith('submitted:')){
                self.selected = ''
                var currentPath=eventName.substring(10)
                app.log('CURRENT PATH: '+currentPath)
                if(currentPath.length>0){
                    self.path = currentPath
                }else{
                    self.path = '/'
                }
                readPaths()
            }else if(eventName.startsWith('cancelled')){
                self.selected = ''
            }else{
                app.log('DOCUMENTS: ' + eventName)
            }
            riot.update()
        });
        
        var readPaths = function () {
            app.log('reading paths ...')
            getData(app.cmAPI+'?pathsonly=true',null,app.user.token,updatePaths,self.listener)
        }

        var readContentList = function () {
            app.log('reading docs ...')
            getData(app.cmAPI+'?path='+self.path+'&language='+self.selectedLanguage+'&status='+self.status,null,app.user.token,updateList,self.listener)
        }

        var updatePaths = function (text) {
            app.log("PATHS: " + text)
            self.paths = JSON.parse(text)
            riot.update()
            if (self.paths.length > 0){
                //self.path = self.paths[0]
                var index = self.paths.indexOf(self.path)
                app.log('PATH INDEX:'+index)
                document.getElementById("pathsDropdown").selectedIndex = index
            } else{
                //self.path = '/'
                document.getElementById("pathsDropdown").selectedIndex = - 1
            }
            readContentList()
            riot.update()
            
        }

        var updateList = function (text) {
            app.log("DOCUMENTS: " + text)
            self.documents = JSON.parse(text)
            riot.update()
            var index = self.statuses.indexOf(self.status)
            document.getElementById("statusesDropdown").selectedIndex = index
            riot.update()
        }

        editDocument(docId, allowEdit){
            return function(e){
                e.preventDefault()
                self.selected = docId
                riot.update()
                app.log('SELECTED FOR EDITING: ' + docId)
                self.refs.doc_edit.init(self.listener, docId, allowEdit, self.selectedLanguage, self.status, self.path)
            }
        }

        setPublished(docId, isPublished){
            return function(e){
                var formData = {
                    'language': self.selectedLanguage,
                    'status': isPublished?'published':'wip'
                }
                sendData(formData,'PUT',app.cmAPI+docId,app.user.token,self.afterPublish,globalEvents)
                riot.update()
            }
        }

        refreshDocs(){
            return function(e){
                app.log('refreshing...')
                readContentList()
            }
        }

        self.afterPublish = function(object){
            var text = '' + object
            app.log('CALBACK: ' + object)
            if (text.startsWith('{')){
                readContentList()
            } else if (text.startsWith('error')){
                //alert(text)
            } else if (text.startsWith('[object MouseEvent')){
                //self.callbackListener.trigger('cancelled')
            }   
        }

        selectLanguage(newLanguage){
            return function(e){
                e.preventDefault()
                self.selectedLanguage = newLanguage
                readContentList()
            }
        }

        self.selectPath = function(e){
            var index = e.target.selectedIndex
            app.log(e.target.options[index].value)
            self.path = e.target.options[index].value
            readContentList()
        }
        
        self.selectStatus = function(e){
            var index = e.target.selectedIndex
            app.log(e.target.options[index].value)
            self.status = e.target.options[index].value
            readContentList()
        }
        
        select(uid){
            return function(e){
                e.preventDefault()
                self.removing = uid
                riot.update()
                app.log('DEL SELECTED ' + uid)
            }
        }
        
        removeDocument(){
            return function(e){
                e.preventDefault()
                app.log('REMOVING ' + self.removing + ' ...')
                deleteData(app.cmAPI + self.removing,app.user.token,self.closeRemove,globalEvents)
            }
        }
        
        self.closeRemove = function(object){
            var text = '' + object
            app.log('CALBACK: ' + object)
            if (text.startsWith('{')){
            //
            } else if (text.startsWith('error:')){
            //it should'n happen
            //alert(text)
            }
            self.removing = ''
            readContentList()
        }

        self.labels = {
            "t_name": {
                "en": "NAME",
                "fr": "NAME",
                "pl": "NAZWA"
            },
                "t_title": {
                "en": "TITLE",
                "fr": "TITLE",
                 "pl": "TYTUŁ"
                },
                "t_status": {
                "en": "STATUS",
                "fr": "STATUS",
                        "pl": "STATUS"
                },
            "path_status": {
                "en": "Path / Status",
                "fr": "Path / Status",
                        "pl": "Ścieżka / Status"
                },
                "title": {
                "en": "documents",
                "fr": "documents",
                        "pl": "dokumenty"
                },
                "remove": {
                "en": "Remove",
                "fr": "Remove",
                        "pl": "Usuń"
                },
                "cancel": {
                "en": "Cancel",
                "fr": "Cancel",
                        "pl": "Porzuć"
                },
                "remove_question": {
                "en": "Do you want to remove selected document?",
                "fr": "Do you want to remove selected document?",
                        "pl": "Czy chcesz usunąć wybrany dokument?"
                },
                "remove_info": {
                "en": "All language versions will be removed.",
                "fr": "All language versions will be removed.",
                        "pl": "Zostaną usunięte wszystkie wersje językowe."
                },
                "remove_title": {
                "en": "Removing document",
                "fr": "Removing document",
                        "pl": "Usuwanie dokumentu"
                }
        }
    </script>
</cm_documents>