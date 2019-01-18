<app_articles>
    <app_404 if={ notFound }></app_404>
    <div class="container top-spacing" if={ !notFound && folder }>
         <div class="row">
            <div class="col-md-12">
                <cs_article2 title={doc.title} summary={doc.summary} type="folder"/>
            </div>
        </div>
        <virtual each={item in list}>
                 <div class="row">
                <div class="col-md-12">
                    <cs_article2 title={item.title} summary={item.summary} type='list' page='#articles' uid={ item.uid }></cs_article2>
                </div>
            </div>
        </virtual>
    </div>
    <div class="container top-spacing" if={ !notFound && !folder }>
        <div class="row">
            <div class="col-md-12">
                <cs_article2 title={doc.title} content={doc.content} type="main" page={ app.currentPage }/>
        </div>
    </div>
</div>
<script>
    var self = this
    self.doc = {}
    self.folder = false
    self.list = []
    self.notFound = false
    self.mounted = false
    globalEvents.on('pageselected', function (event) {
        if (self.mounted && app.previousPath && app.previousPath != app.docPath) {
            //console.log('reloading content ' + self.mounted)
            getData(app.csAPI + app.docPath + '?language=' + app.language, null, null, setDocument, self, null)
        }
    })
    globalEvents.on('language', function (event) {
        if(self.mounted){ 
            getData(app.csAPI + app.docPath + '?language=' + app.language, null, null, setDocument, self, null) 
        }
    })
    self.on('*', function (event) {
        switch (event) {
            case 'err:404':
                self.notFound = true
                self.update()
                break
            case 'mount':
                self.mounted = true
                getData(app.csAPI + app.docPath + '?language=' + app.language, null, null, setDocument, self, null)
                break
            case 'unmount':
                self.mounted = false
        }
    })
    var setDocument = function (text) {
        self.doc = JSON.parse(text)
        //console.log(self.doc)
        self.notFound = false
        self.folder = (self.doc.mimeType == 'application/x-folder')
        riot.mount('cs_article2', {title: self.doc.title, content: self.doc.content, type: 'main'})
        self.update()
        if (self.folder) {
            getData(app.csAPI + '?path=' + app.docPath + '/&language=' + app.language, null, null, setDocList, self, null)
        }
    }
    var setDocList = function (text) {
        self.list = JSON.parse(text)
        //console.log(self.list)
        self.notFound = false
        self.update()
    }
</script>
</app_articles>
