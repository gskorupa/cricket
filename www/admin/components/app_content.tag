<app_content>
    <main>
        <div  class="container-fluid topspacing" if={ app.currentPage=='main'}><app_main></app_main></div>
        <div  class="container-fluid topspacing" if={ app.currentPage=='documents'}><cm_documents></cm_documents></div>
        <div  class="container-fluid topspacing" if={ app.currentPage=='tags'}><cm_tags></cm_tags></div>
        <div  class="container-fluid topspacing" if={ app.currentPage=='users'}><app_users></app_users></div>
        <div  class="container-fluid topspacing" if={ app.currentPage=='login'}><app_login></app_login></div>
        <div  class="container-fluid topspacing" if={ app.currentPage=='logout'}><app_logout></app_logout></div>
    </main>
</app_content>