<cs_article2>
    <article class="{ opts.type }">
        <header>
            <h1><raw html="{article.title}"/></h1>
        </header>
        <section if="{opts.type=='list'}"><raw html="{article.summary}"/></section>
        <section if="{opts.type=='folder'||opts.type=='main'}"><raw html="{article.content}"/></section>
        <p if="{ opts.type=='list' && opts.page && opts.uid }" class="top-spacing">
            <a class="btn btn-outline-secondary" href="{ article.link }" role="button">{ app.texts.details } &raquo;</a>
        </p>
        <p if="{ opts.type=='main' && opts.page }" class="top-spacing">
            <a class="btn btn-outline-secondary" onclick="window.location.replace('#!'+app.previousId);" role="button">&laquo; { app.texts.back }</a>
        </p>
    </article>
    <script charset="UTF-8">
        var self = this
        self.opts = opts
        self.article = {
            title: '',
            summary: '',
            content: '',
            link: ''
        }
        
        self.on('mount', function (event) {
            self.decode()
        })
        self.on('update', function (event) {
            self.decode()
        })
        
        self.decode=function(){
            if (opts.title) {
                try {
                    self.article.title = decodeURIComponent(opts.title)
                } catch (e) {
                    self.article.title = unescape(opts.title)
                }
            }
            if (opts.page && opts.uid) {
                self.article.link = (opts.page + opts.uid).replace(/\//g, ',')
            }
            if (opts.summary) {
                try {
                    self.article.summary = decodeURIComponent(opts.summary)
                } catch (e) {
                    self.article.summary = unescape(opts.summary)
                }
            }
            if (opts.content) {
                try {
                    self.article.content = decodeURIComponent(opts.content)
                } catch (e) {
                    self.article.content = unescape(opts.content)
                }
            }
        }
    </script>
</cs_article2>