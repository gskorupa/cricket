<app-homelist>
    <div class="row">
        <div each="{item in opts.list}" class="col-md-4">
            <cs_article2 title="{item.title}" summary="{item.summary}" content="{item.content}" type='list' page='#articles' uid="{ item.uid }" />
        </div>
    </div>
</app-homelist>