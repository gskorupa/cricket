<cs_article2>
    <article class={ opts.type }>
        <header>
            <h1 ref='title'></h1>
            <div ref='summary'></div>
        </header>
        <p if={ opts.type=='list' && opts.page && opts.uid }><a class="btn btn-secondary" href={ article.link } role="button">{ text.details[app.language] } &raquo;</a></p>
        <section ref='content'></section>
        <p if={ opts.type=='main' && opts.page }><a class="btn btn-secondary" onclick="history.back()" role="button">&laquo; { text.back[app.language] }</a></p>
    </article>
    <script charset="UTF-8">
        var self = this
        self.opts = opts
        self.article = {}
        globalEvents.on('pageselected', function(event){
            if (opts.page && opts.uid) {
                self.article.link = (opts.page + opts.uid).replace(/\//g, ',')
            }
            self.update()
        })
        self.on('mount', function (event) {
            if (opts.title) {
                try {
                    self.article.title = decodeURIComponent(opts.title)
                } catch (e) {
                    self.article.title = unescape(opts.title)
                }
                self.refs.title.innerHTML = self.article.title
            }
            if (opts.content && opts.type=='main') {
                try {
                    self.article.content = decodeURIComponent(opts.content)
                } catch (e) {
                    self.article.content = unescape(opts.content)
                }
                self.refs.content.innerHTML = self.article.content
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
                self.refs.summary.innerHTML = self.article.summary
            }
            self.update()
        })
        self.text = {
            "details": {
                "en": "View details",
                "fr": "View details",
                "pl": "Więcej"
            },
            "back": {
                "en": "Back",
                "fr": "Back",
                "pl": "Wróć"
            }
        }
    </script>
</cs_article2>