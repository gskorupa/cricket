<app-articles>
    <app-404 if="{ !found }"/>
    <div class="container top-spacing" show="{ found && folder }">
        <div class="row">
            <div class="col-md-12">
                <cs_article2 ref='folderart' title="{doc.title}" summary="{doc.summary}" content="{doc.content}" type="folder" page="{ app.currentPage }" uid={doc.uid}/>
            </div>
        </div>
        <app-artlist ref='artlist' list={list}/>
    </div>
    <div class="container top-spacing" show="{ found && !folder }">
        <div class="row">
            <div class="col-md-12">
                <cs_article2 ref='mainart' title="{doc.title}" summary="{doc.summary}" content="{doc.content}" type="main" page="{ app.currentPage }" uid={doc.uid}/>
            </div>
        </div>
    </div>
    <script>
        var self = this
        self.doc = {title: 'about', summary: ''}
        self.folder = false
        self.list = []
        self.found = true

        globalEvents.on('pageselected', function (event) {
            loadDoc()
        })
        globalEvents.on('update', function (event) {
            try {
                //if(self.folder){
                self.refs.folderart.update({title: self.doc.title, summary: self.doc.summary, content: self.doc.content, type: "folder"})
                self.refs.artlist.update({list: self.list})
                //}else{    
                self.refs.mainart.update({title: self.doc.title, summary: self.doc.summary, content: self.doc.content, type: "main", page: app.currentPage})
                //}
            } catch (event) {
            }
            riot.update()
        })
        globalEvents.on('mount', function (event) {
            try {
                //if(self.folder){
                self.refs.folderart.update({title: self.doc.title, summary: self.doc.summary, content: self.doc.content, type: "folder"})
                self.refs.artlist.update({list: self.list})
                //}else{    
                self.refs.mainart.update({title: self.doc.title, summary: self.doc.summary, content: self.doc.content, type: "main", page: app.currentPage})
                //}
            } catch (event) {
            }
        })
        self.on('err:404', function (event) {
            self.found = false
            self.update()
        })

        var loadDoc = function (text) {
            if (app.currentPage.startsWith('articles')) {
                getData(app.csAPI + app.docPath + '?language=' + app.language, null, null, setDocument, self, null)
            }
        }
        var setDocument = function (text) {
            self.doc = JSON.parse(text)
            self.found = true
            self.folder = (self.doc.mimeType == 'application/x-folder')
            self.update()
            if (self.folder) {
                getData(app.csAPI + '?path=/' + app.docPath + '/&language=' + app.language, null, null, setDocList, self, null)
            }
        }
        var setDocList = function (text) {
            self.list = JSON.parse(text)
            self.update()
            setTimeout(function () {
                riot.update()
            }, 250);
        }
    </script>
</app-articles>
