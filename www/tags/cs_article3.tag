<cs_article3>
    <h1 class='display-3'><raw html="{article.title}"/></h1>
    <p><raw html="{article.summary}"/></p>
    <p ><a class="btn btn-outline-dark btn-lg" href="{article.link}" role="button">{ app.texts.details } &raquo;</a></p>    
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
        }
    </script>
</cs_article3>