<app-home>
    <div class="jumbotron" show={app.currentPage==''}>
        <div class="container">
            <cs_article3 ref='mainarticle' title={doc.title} summary={doc.summary} uid={doc.uid} page='#articles'/>
        </div>
    </div>
    <div class="container" show={app.currentPage==''}>
        <app-homelist ref='homelist' list={list}/>
    </div>
    <script>
    var self=this
    self.doc = {
            title: 'Hello', 
            summary: 'It seems that your application is not configured. More information: <a href="https://github.com/gskorupa/Cricket/wiki/Microsite-Quickstart">Microsite Quickstart</a>',
            uid:''
        }
        self.list = []

    globalEvents.on('pageselected', function (event) {
    if (app.currentPage=='') {
                loadDocs()
            }
    })
    self.on('mount', function (event) {
        try{
            self.refs.mainarticle.update({title:self.doc.title, summary:self.doc.summary, uid:self.doc.uid, page:'#articles'})
        }catch(event){}
        try{
            self.refs.homelist.update({list:self.list})
        }catch(event){}
    })
    self.on('update', function (event) {
        try{
            self.refs.mainarticle.update({title:self.doc.title, summary:self.doc.summary, uid:self.doc.uid, page:'#articles'})
        }catch(event){}
        try{
            self.refs.homelist.update({list:self.list})
        }catch(event){}
    })
    var loadDocs = function () {
            getData(app.csAPI + 'home?language=' + app.language, null, null, setDocument, self, null)
        }
    var setDocument = function (text) {
            self.doc = JSON.parse(text)
            self.update()
            getData(app.csAPI + '?path=/home/&language=' + app.language, null, null, setDocList, self, null)
        }
    var setDocList = function (text) {
            self.list = JSON.parse(text)
            if (self.list) {
                self.list = self.list.slice(0, 3)
            }
            self.update()
        }
</script>
</app-home>