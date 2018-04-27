<cs_article>
    <article class='standard'>
        <header>
            <h1>{title}</h1>
            <div class='intro' if={summary}><raw html={summary}/></div>
        </header>
        <div if={content}><raw html={content}/></div>
        <footer><div class="author'>{ author }</div><div class="published'>{ published }</div></footer>
        <div if={ mode=='view' }><a href="#" onClick="history.back()" }>{ texts.back[app.language]}</a></div>
        <div  if={ mode=='list' }><a href={detailsLink}>{ texts.details[app.language]}</a></div>
    </article>
    <script>
        var self=this
        self.title=opts.title
        self.summary=opts.summary
        self.content=opts.content
        self.author=opts.author
        self.published=opts.published
        self.type=opts.type
        self.page=opts.page
        self.uid=opts.uid
        if(opts.mode){
        self.mode=opts.mode
        }else{
        self.mode='default'
        }
        self.detailsLink=''+self.page+self.uid
        self.detailsLink=self.detailsLink.replace(/\//g , ',')
        
        self.texts = {
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
</cs_article>