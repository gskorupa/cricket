<cm_document_form>
    <div class="panel panel-default">
        <div class="panel-heading" if={ self.mode == 'create' }><h2>{ app.texts.cm_document_form.document_new[app.language] } [{ doc.status }]</h2></div>
        <div class="panel-heading" if={ self.mode == 'update' }><h2>{ app.texts.cm_document_form.document_modify[app.language] } [{ doc.status }]</h2></div>
        <div class="panel-heading" if={ self.mode == 'view' }><h2>{ app.texts.cm_document_form.document_view[app.language] } [{ doc.status }]</h2></div>
        <div class="panel-body">
            <form onsubmit={ self.submitForm }>
                <div class="form-group">
                    <label for="type">{ app.texts.cm_document_form.type[app.language] }</label>
                    <select class="form-control" id="type" name="type" value={ doc.type } onchange={ self.changeType } readonly={ self.mode != 'create'  } required>
                        <option value="ARTICLE">ARTICLE</option>
                        <option value="CODE">CODE</option>
                        <option value="FILE">FILE</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="name">{ app.texts.cm_document_form.name[app.language] }</label>
                    <input class="form-control" id="name" name="name" type="text" value={ doc.name } readonly={ self.mode != 'create'  } required>
                </div>
                <div class="form-group">
                    <label for="path">{ app.texts.cm_document_form.path[app.language] }</label>
                    <input class="form-control" id="path" name="path" type="text" value={ doc.path } readonly={ self.mode != 'create'  } >
                </div>
                <div class="form-group">
                    <label for="uid">{ app.texts.cm_document_form.uid[app.language] }</label>
                    <input class="form-control" id="uid" name="uid" type="text" value={ doc.uid } readonly={ true }>
                </div>
                <div class="form-group">
                    <label for="author">{ app.texts.cm_document_form.author[app.language] }</label>
                    <input class="form-control" id="author" name="author" type="text"  value={ doc.author } readonly={ !allowEdit }>
                </div>
                <div class="form-group">
                    <label for="title">{ app.texts.cm_document_form.title[app.language] }</label>
                    <input class="form-control" id="title" name="title" type="text" value={ doc.title } readonly={ !allowEdit }>
                </div>
                <div class="form-group" if={doc.type=='ARTICLE'}>
                    <label for="summary">{ app.texts.cm_document_form.summary[app.language] }</label>
                    <textarea class="form-control cricket-article" rows="3" id="summary" name="summary" readonly={ !allowEdit }>{ doc.summary }</textarea>
                </div>
                <div class="form-group" if={doc.type!='ARTICLE'}>
                    <label for="summary">{ app.texts.cm_document_form.summary[app.language] }</label>
                    <textarea class="form-control" rows="3" id="summary" name="summary" readonly={ !allowEdit }>{ doc.summary }</textarea>
                </div>
                <div class="form-group" if={ doc.type != 'FILE' }>
                    <label for="mimeType">{ app.texts.cm_document_form.mimeType[app.language] }</label>
                    <input class="form-control" id="mimeType" name="mimeType" type="text" value={ doc.mimeType }>
                </div>
                <div class="form-group" if={ doc.type == 'FILE' }>
                    <label for="mimeType">{ app.texts.cm_document_form.mimeType[app.language] }</label>
                    <input class="form-control" id="mimeType" name="mimeType" type="text" value={ doc.mimeType } readonly>
                </div>
                <div class="form-group" if={ doc.type != 'FILE' && doc.type == 'ARTICLE'}>
                    <label for="content">{ app.texts.cm_document_form.content[app.language] }</label>
                    <textarea class="form-control cricket-article" style="white-space:pre-wrap"
                              rows="10" id="content" name="content" readonly={ !allowEdit }>{ doc.content }</textarea>
                </div>
                <div class="form-group" if={ doc.type != 'FILE' && doc.type != 'ARTICLE'}>
                    <label for="content">{ app.texts.cm_document_form.content[app.language] }</label>
                    <textarea class="form-control" style="white-space:pre-wrap"
                              rows="10" id="content" name="content" readonly={ !allowEdit }>{ doc.content }</textarea>
                </div>
                <div class="form-group" if={doc.type == 'FILE'}>
                    <label for="content">{ app.texts.cm_document_form.content[app.language] }</label>
                    <input class="form-control" id="content" name="content" value={ doc.content } readonly>
                </div>
                <div class="form-group">
                    <label for="doctags">{ app.texts.cm_document_form.doctags[app.language] }</label>
                    <input class="form-control" id="doctags" name="doctags" type="text" value={ doc.tags } >
                </div>
                <div class="form-group">
                    <label for="doctags">{ app.texts.cm_document_form.extra[app.language] }</label>
                    <input class="form-control" id="extra" name="extra" type="text" value={ doc.extra } >
                </div>
                <div class="form-group">
                    <label for="language">{ app.texts.cm_document_form.language[app.language] }</label>
                    <input class="form-control" id="language" name="language" type="text" value={ doc.language } readonly={ true }>
                </div>
                <!--
                <div class="form-group">
                    <label for="commentable">{ app.texts.cm_document_form.commentable[app.language] }</label>
                    <input class="form-control" id="commentable" name="commentable" type="text" value={ doc.commentable } readonly={ !allowEdit }>
                </div>
                -->
                <div class="form-group" if={doc.type == 'FILE' && allowEdit}>
                    <label for="file">{ app.texts.cm_document_form.selectFile[app.language]}</label><br />
                    <input class="form-control-file" type="file" name="file" id="file">
                </div>
                <button type="submit" class="btn btn-primary" disabled={ !allowEdit }>{ app.texts.cm_document_form.save[app.language] }</button>
                <button type="button" onclick={ close } class="btn btn-secondary">{ app.texts.cm_document_form.cancel[app.language] }</button>
            </form>
        </div>
</div>
<script>
    this.visible = true
    self = this
    self.listener = riot.observable()
    self.callbackListener
    self.allowEdit = false
    self.method = 'POST'
    self.mode = 'view'
    self.selectedLanguage = app.language
    self.selectedStatus = 'wip'
    self.doc = {
        'uid': '',
        'author': '',
        'name': '',
        'path': '',
        'title': '',
        'summary': '',
        'content': '',
        'tags': '',
        'language': '',
        'type': 'ARTICLE',
        'mimeType': '',
        'status': '',
        'created': '',
        'modified': '',
        'published': '',
        'createdBy': '',
        'commentable': false,
        'size': 0,
        'extra': ''
    }

    init(eventListener, uid, editable, language, status, path){
        self.callbackListener = eventListener
        self.allowEdit = editable
        self.selectedLanguage = language
        self.selectedStatus = status
        self.doc.language = language
        self.doc.path = path
        self.doc.status = status
        self.doc.mimeType = 'text/html'
        self.method = 'POST'
        app.log('HELLO ' + uid)
        app.log('CALLBACK: ' + self.callbackListener)
        app.log('EDITABLE: ' + self.allowEdit)
        if (uid != 'NEW') {
            readDocument(uid)
            self.method = 'PUT'
            if (self.allowEdit) {
                self.mode = 'update'
            } else {
                self.mode = 'view'
            }
        } else {
            self.mode = 'create'
            self.selectedStatus = 'wip'
            self.doc.status = 'wip'
        }
        $(function() {
            $(".cricket-article").htmlarea({
                loaded: function() {
                this.updateTextArea();
                this.updateHtmlArea();
                this.showHTMLView();
                }
            })
        })
    }

    self.changeType = function(e){
        e.preventDefault()
        self.doc.type = e.target.value
        riot.update()
        $(function() {
            $(".cricket-article").htmlarea({
                loaded: function() {
                this.updateTextArea();
                this.updateHtmlArea();
                this.showHTMLView();
                }
            })
        })
    }
    
    self.submitForm = function (e) {
        e.preventDefault()

        docPath = ''
        if (e.target.elements['uid']) {
            docPath = (self.method == 'PUT') ? e.target.elements['uid'].value : ''
        }

        var fd = new FormData(e.target)
        var pth = ''
        if(fd.get('path')){
            pth=fd.get('path').trim()
        }
        var name=fd.get('name').trim()
        if (!pth.endsWith('/')) {
            pth = pth + '/'
        }
        fd.set('path',pth)
        while(name.startsWith('/')){
            name=name.substring(1)
        }
        while(name.endsWith('/')){
            name=name.substring(0,name.length-1)
        }
        fd.set('name', name)
        fd.set('uid',pth+name)
        var title=encodeURIComponent(fd.get('title'))
        fd.set('title',title)
        var summ=encodeURIComponent(fd.get('summary'))
        fd.set('summary',summ)
        var c=encodeURIComponent(fd.get('content'))
        fd.set('content',c)
        fd.set('tags',fd.get('doctags'))
        fd.set('extra',fd.get('extra'))
        sendFormData(fd, self.method, app.cmAPI + docPath, app.user.token, self.close, self.listener)
    }
    
    self.listener.on('*', function(event, originalEvent){
        try{
            var status = originalEvent.currentTarget.status
            var message = originalEvent.currentTarget.statusText
            if(status == 409){
                alert('Error while saving the document\nThe same UID already exists.')
            }else if(status!=200 && status!=201){
                alert('Error while saving the document\nCode '+status+': '+message)
            }
        }catch(err){
            app.log(err)
        }
      riot.update()
    })

    self.close = function (object) {
        var text = '' + object
        app.log('CALBACK: ' + object)
        if (text.startsWith('{')) {
            var tmpDoc = self.doc = JSON.parse(text)
            self.callbackListener.trigger('submitted:'+tmpDoc.path)
        } else if (text.startsWith('[object MouseEvent')) {
            self.callbackListener.trigger('cancelled')
        } else if (text.startsWith('error:409')){
                alert('This UID is already defined!')
        } else if (text.startsWith('error')) {
            alert(text)
        }
    }

    var update = function (text) {
        app.log("DOC: " + text)
        self.doc = JSON.parse(text);
        try{
            self.doc.title=decodeURIComponent(self.doc.title)
        }catch(e){
            self.doc.title=unescape(self.doc.title)
        }
        try{
            self.doc.summary=decodeURIComponent(self.doc.summary)
        }catch(e){
            self.doc.summary=unescape(self.doc.summary)
        }
        try{
            self.doc.content=decodeURIComponent(self.doc.content)
        }catch(e){
            self.doc.content=unescape(self.doc.content)
        }
        riot.update();
    }

    var readDocument = function (docUid) {
        getData(app.cmAPI+docUid+'?language='+self.selectedLanguage+'&status='+self.selectedStatus,null,app.user.token,update,self.listener)
    }

</script>
</cm_document_form>
