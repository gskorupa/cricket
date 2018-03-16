<cs_article>
    <article>
        <header>
            <h1 ref="a_title" if={ article.title }>{ article.title }</h1>
            <p ref="a_summary" if={ article.summary }>{ article.summary }</p>
        </header>
        <section ref="a_content">{ article.content }</section>
    </article>
    <script charset="UTF-8">
        var self = this;
        self.opts = opts
        self.listener = riot.observable()
        self.article = {
            title: '',
            summary:'',
            content: 'loading content '+opts.path+' ...'
        }
        this.on('mount',function(){
            app.log('ARTICLE MOUNT')
            app.log(self.refs)
        })
        self.listener.on('*',function(){
            riot.update()
        })
        this.on('unmount',function(){
            app.log('ARTICLE UNMOUNT')
        })     
        self.updateContent = function(){
            readDocument(self.opts.path, self.opts.language, self.showMe)
        }
        
        readDocument = function (path, language, callback) {
            getData(app.csAPI+path+'?language='+language,null,null,callback,self.listener)
        }

        self.showMe = function (response) {
            var doc = JSON.parse(response);
            self.article['title'] = doc.title
            self.article['summary'] = doc.summary
            self.article['content'] = unescape(doc.content)
            riot.update()
            if(self.article['title']){
                self.refs.a_title.innerHTML=self.article['title']
            }
            if(self.article['summary']){
                self.refs.a_summary.innerHTML=self.article['summary']
            }
            self.refs.a_content.innerHTML=self.article['content']
        }
    </script>
</cs_article>