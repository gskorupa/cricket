<cs_article2>
    <article class={ opts.type }>
        <header>
            <h1 if={ opts.title }>{ article.title }</h1>
            <div if={ opts.summary }>{ article.summary }</div>
        </header>
        <p if={ opts.type=='list' && article.link }><a class="btn btn-secondary" href={ article.link } role="button">{ text.details[app.language] } &raquo;</a></p>
        <section if={ opts.type=='main'}>{ article.content }</section>
        <p if={ opts.type=='main' && opts.page }><a class="btn btn-secondary" onclick="history.back()" role="button">&laquo; { text.back[app.language] }</a></p>
    </article>
    <script charset="UTF-8">
        var self = this
        self.opts = opts
        self.article = {}
        if (opts.title) {
            try {
                self.article.title = decodeURIComponent(opts.title)
            } catch (e) {
                self.article.title = unescape(opts.title)
            }
        }
        if (opts.content) {
            try {
                self.article.content = decodeURIComponent(opts.content)
            } catch (e) {
                self.article.content = unescape(opts.content)
            }
        }
        if (opts.page && opts.uid) {
            self.article.link = (opts.page + opts.uid).replace(/\//g, ',')
            //self.article.link.replace(/\//g,',')
        }
        if (opts.summary) {
            try {
                self.article.summary = decodeURIComponent(opts.summary)
            } catch (e) {
                self.article.summary = unescape(opts.summary)
            }
        }
        /*
         self.on('mount', function (event) {
         console.log('mount cs_article2')
         console.log(opts)
         })
         self.on('unmount', function (event) {
         console.log('unmount cs_article2')
         })
         */
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