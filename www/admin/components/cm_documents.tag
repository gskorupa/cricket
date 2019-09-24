<cm_documents>
    <div class="row" if={ selected }>
         <div class="col-md-12">
            <cm_document_form ref="doc_edit"></cm_document_form>
        </div>
    </div>
    <div class="row" if={ !selected }>
        <div class="col-md-12">
            <h2>{app.texts.cm_documents.title[app.language]} 
                <virtual each={ lang, i in app.languages}>
                    <button type="button" class="btn btn-sm { lang==selectedLanguage?'btn-primary':'btn-secondary' }" onclick={ selectLanguage(lang) }>{ lang }</button>
                </virtual>
                <i class="material-icons clickable" onclick={ refreshDocs() }>refresh</i>
                <i class="material-icons clickable" onclick={ editDocument('NEW', true) }>add</i>
            </h2>
        </div>
    </div>
    <div class="row" if={ !selected }>
        <div class="col-md-12">
            <form class="form-inline">
                <label class="mr-2" for="pathsDropdown">{ app.texts.cm_documents.path_status[app.language] }</label>
                <select class="select mr-2" id="pathsDropdown" onchange={ selectPath }>
                    <option each={ tmpPath, index in paths }>{ tmpPath }</option>
                </select>
                <select class="select" id="statusesDropdown" onchange={ selectStatus }>
                    <option each={ tmpStatus, index in statuses }>{ tmpStatus }</option>
                </select>
                <label for="doctag" style="margin-left:10px;margin-right: 5px;">{ app.texts.cm_documents.doctag[app.language] }</label>
                <input class="form-control" id="doctag" name="doctag" type="text" value={ doctag }>
            </form>
        </div>
    </div>
    <div class="row" if={ !selected }>
        <div class="col-md-12">
            <table id="doclist" class="table table-condensed topspacing-sm">
                <thead>
                    <tr class="d-flex">
                        <th class="col-1">{app.texts.cm_documents.t_type[app.language]}</th>
                        <th class="col-3">{app.texts.cm_documents.t_name[app.language]}</th>
                        <th class="col-5">{app.texts.cm_documents.t_title[app.language]}</th>
                        <!--<th>{app.texts.cm_documents.t_status[app.language]}</th>-->
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
                            <i class="material-icons clickable" if={ doc.rights=='rw'} onclick={ editDocument(doc.uid, true) }>mode_edit</i>
                            <i class="material-icons clickable" if={ doc.status=='wip' && doc.rights=='rw'} onclick={ setPublished(doc.uid, true) }>visibility</i>
                            <i class="material-icons clickable" if={ doc.status=='published' && doc.rights=='rw'} onclick={ setPublished(doc.uid, false) }>visibility_off</i>
                            <i class="material-icons clickable" if={ doc.rights=='rw'} onclick={ select(doc.uid) } data-toggle="modal" data-target="#removeDialog">delete</i>
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
                            <h4 class="modal-title">{app.texts.cm_documents.remove_title[app.language]}</h4>
                        </div>
                        <div class="modal-body">
                            <p>{app.texts.cm_documents.remove_question[app.language]}</p>
                            <p class="text-warning"><small>{app.texts.cm_documents.remove_info[app.language]}</small></p>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-primary" data-dismiss="modal" onclick={ removeDocument() }>{app.texts.cm_documents.remove[app.language]}</button>
                            <button type="button" class="btn btn-secondary" data-dismiss="modal" onclick={ select('') }>{app.texts.cm_documents.cancel[app.language]}</button>
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
        self.doctag = ''
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
                riot.update()
                updateSelectors()
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
            self.doctag=document.getElementById("doctag").value.trim()
            var query = app.cmAPI+'?path='+self.path+'&language='+self.selectedLanguage+'&status='+self.status
            if(self.doctag.length>0){
                query=query+'&tag='+self.doctag
            }
            getData(query,null,app.user.token,updateList,self.listener)
        }

        var updatePaths = function (text) {
            self.paths = JSON.parse(text)
            riot.update()
            //if (self.paths.length > 0){
            //    var index = self.paths.indexOf(self.path)
            //    document.getElementById("pathsDropdown").selectedIndex = index
            //} else{
            //    document.getElementById("pathsDropdown").selectedIndex = - 1
            //}
            updateSelectors()
            readContentList()
            riot.update()
        }
        
        var updateSelectors = function(){
            if (self.paths.length > 0){
                var index = self.paths.indexOf(self.path)
                document.getElementById("pathsDropdown").selectedIndex = index
            } else{
                document.getElementById("pathsDropdown").selectedIndex = - 1
            }
            var index2 = self.statuses.indexOf(self.status)
            document.getElementById("statusesDropdown").selectedIndex = index2
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
            if (text.startsWith('{')){
                readContentList()
            } else if (text.startsWith('error')){
            } else if (text.startsWith('[object MouseEvent')){
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

        
    </script>
</cm_documents>