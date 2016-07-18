.class public Lkr/ac/kaist/wala/hybridroid/test/annotation/MainActivity;
.super Landroid/app/Activity;
.source "MainActivity.java"


# instance fields
.field private wv:Landroid/webkit/WebView;


# direct methods
.method public constructor <init>()V
    .locals 0

    .prologue
    .line 9
    invoke-direct {p0}, Landroid/app/Activity;-><init>()V

    return-void
.end method


# virtual methods
.method protected onCreate(Landroid/os/Bundle;)V
    .locals 3
    .param p1, "savedInstanceState"    # Landroid/os/Bundle;

    .prologue
    .line 15
    invoke-super {p0, p1}, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V

    .line 16
    const v0, 0x7f030019

    invoke-virtual {p0, v0}, Lkr/ac/kaist/wala/hybridroid/test/annotation/MainActivity;->setContentView(I)V

    .line 18
    const v0, 0x7f090050

    invoke-virtual {p0, v0}, Lkr/ac/kaist/wala/hybridroid/test/annotation/MainActivity;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/webkit/WebView;

    iput-object v0, p0, Lkr/ac/kaist/wala/hybridroid/test/annotation/MainActivity;->wv:Landroid/webkit/WebView;

    .line 19
    iget-object v0, p0, Lkr/ac/kaist/wala/hybridroid/test/annotation/MainActivity;->wv:Landroid/webkit/WebView;

    invoke-virtual {v0}, Landroid/webkit/WebView;->getSettings()Landroid/webkit/WebSettings;

    move-result-object v0

    const/4 v1, 0x1

    invoke-virtual {v0, v1}, Landroid/webkit/WebSettings;->setJavaScriptEnabled(Z)V

    .line 20
    iget-object v0, p0, Lkr/ac/kaist/wala/hybridroid/test/annotation/MainActivity;->wv:Landroid/webkit/WebView;

    new-instance v1, Landroid/webkit/WebChromeClient;

    invoke-direct {v1}, Landroid/webkit/WebChromeClient;-><init>()V

    invoke-virtual {v0, v1}, Landroid/webkit/WebView;->setWebChromeClient(Landroid/webkit/WebChromeClient;)V

    .line 21
    iget-object v0, p0, Lkr/ac/kaist/wala/hybridroid/test/annotation/MainActivity;->wv:Landroid/webkit/WebView;

    new-instance v1, Landroid/webkit/WebViewClient;

    invoke-direct {v1}, Landroid/webkit/WebViewClient;-><init>()V

    invoke-virtual {v0, v1}, Landroid/webkit/WebView;->setWebViewClient(Landroid/webkit/WebViewClient;)V

    .line 22
    iget-object v0, p0, Lkr/ac/kaist/wala/hybridroid/test/annotation/MainActivity;->wv:Landroid/webkit/WebView;

    new-instance v1, Lkr/ac/kaist/wala/hybridroid/test/annotation/JSBridge;

    invoke-direct {v1}, Lkr/ac/kaist/wala/hybridroid/test/annotation/JSBridge;-><init>()V

    const-string v2, "bridge"

    invoke-virtual {v0, v1, v2}, Landroid/webkit/WebView;->addJavascriptInterface(Ljava/lang/Object;Ljava/lang/String;)V

    .line 23
    return-void
.end method

.method protected onResume()V
    .locals 2

    .prologue
    .line 27
    invoke-super {p0}, Landroid/app/Activity;->onResume()V

    .line 28
    iget-object v0, p0, Lkr/ac/kaist/wala/hybridroid/test/annotation/MainActivity;->wv:Landroid/webkit/WebView;

    const-string v1, "file:///android_asset/index.html"

    invoke-virtual {v0, v1}, Landroid/webkit/WebView;->loadUrl(Ljava/lang/String;)V

    .line 29
    return-void
.end method
