<app-artlist>
    <div class="row" each="{item in opts.list}">
        <div class="col-md-12">
            <cs_article2 title="{item.title}" summary="{item.summary}" content="{item.content}" type='list' page='#articles' uid="{ item.uid }" />
        </div>
    </div>
</app-artlist>