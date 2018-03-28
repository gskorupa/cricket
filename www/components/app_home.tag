<app_home>
    <!-- Main jumbotron for a primary marketing message or call to action -->
    <div class="jumbotron">
        <div class="container">
            <cs_article3 title={doc.title} summary={doc.summary} uid={doc.uid} page='#articles'/>
        </div>
    </div>
    <div class="container">
        <!-- Example row of columns -->
        <div class="row">
            <virtual each={item in list}>
                     <div class="col-md-4">
                    <cs_article2 title={item.title} summary={item.summary} type='list' page='#articles' uid={ item.uid }></cs_article2>
                </div>
            </virtual>
        </div>
    </div>
    <script>
        var self = this
        self.mounted = false
        self.doc = {title: 'Hello', summary: 'It seems that your application is not configured.'}
        self.list = []
        globalEvents.on('language', function (event) {
            if(self.mounted){
                loadDocs()
            }
        })
        globalEvents.on('pageselected', function (event) {
            if (self.mounted && app.currentPage == '') {
                loadDocs()
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
                    loadDocs()
                    break
                case 'unmount':
                    self.mounted = false
            }
        })
        var loadDocs = function () {
            getData(app.csAPI + '/home?language=' + app.language, null, null, setDocument, self, null)
            getData(app.csAPI + '?path=/home/&language=' + app.language, null, null, setDocList, self, null)
        }
        var setDocument = function (text) {
            self.doc = JSON.parse(text)
            riot.mount('cs_article3', {title: self.doc.title, summary: self.doc.summary, page: '#articles', uid: self.doc.uid})
            self.update()
        }
        var setDocList = function (text) {
            self.list = JSON.parse(text)
            if (self.list) {
                self.list = self.list.slice(0, 3)
            }
            self.update()
        }
    </script>
</app_home>