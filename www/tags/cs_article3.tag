<cs_article3>
    <h1 ref='title' class='display-3'></h1>
    <p ref='summary'></p>
    <p ><a class="btn btn-primary btn-lg" href={article.link} role="button">{ text.details[app.language] } &raquo;</a></p>    
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
            if (opts.title) {
                try {
                    self.article.title = decodeURIComponent(opts.title)
                } catch (e) {
                    self.article.title = unescape(opts.title)
                }
                self.refs.title.innerHTML = self.article.title
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
            }
        }
    </script>
</cs_article3>