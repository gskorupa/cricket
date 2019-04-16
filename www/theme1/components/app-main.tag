<app-main>
    <main role="main">
        <app-home show="{ app.currentPage=='' }"/>
        <app-articles show="{ app.currentPage.startsWith('articles')}"/>
        <app-search show="{ app.currentPage.startsWith('search')}"/>
    </main>
</app-main>