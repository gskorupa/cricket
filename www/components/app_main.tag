<app_main>
    <main role="main">
        <app_home if={ app.currentPage == ''}></app_home>
        <app_articles if={ app.currentPage.startsWith('articles')}></app_articles>
    </main>
</app_main>
