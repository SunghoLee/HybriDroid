.class Landroid/support/v4/media/MediaBrowserServiceCompatApi21$MediaBrowserServiceAdaptorApi21$ServiceBinderProxyApi21;
.super Landroid/support/v4/media/IMediaBrowserServiceAdapterApi21$Stub;
.source "MediaBrowserServiceCompatApi21.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Landroid/support/v4/media/MediaBrowserServiceCompatApi21$MediaBrowserServiceAdaptorApi21;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x8
    name = "ServiceBinderProxyApi21"
.end annotation


# instance fields
.field final mServiceImpl:Landroid/support/v4/media/MediaBrowserServiceCompatApi21$ServiceImplApi21;


# direct methods
.method constructor <init>(Landroid/support/v4/media/MediaBrowserServiceCompatApi21$ServiceImplApi21;)V
    .locals 0
    .param p1, "serviceImpl"    # Landroid/support/v4/media/MediaBrowserServiceCompatApi21$ServiceImplApi21;

    .prologue
    .line 129
    invoke-direct {p0}, Landroid/support/v4/media/IMediaBrowserServiceAdapterApi21$Stub;-><init>()V

    .line 130
    iput-object p1, p0, Landroid/support/v4/media/MediaBrowserServiceCompatApi21$MediaBrowserServiceAdaptorApi21$ServiceBinderProxyApi21;->mServiceImpl:Landroid/support/v4/media/MediaBrowserServiceCompatApi21$ServiceImplApi21;

    .line 131
    return-void
.end method


# virtual methods
.method public addSubscription(Ljava/lang/String;Ljava/lang/Object;)V
    .locals 2
    .param p1, "id"    # Ljava/lang/String;
    .param p2, "callbacks"    # Ljava/lang/Object;

    .prologue
    .line 145
    iget-object v0, p0, Landroid/support/v4/media/MediaBrowserServiceCompatApi21$MediaBrowserServiceAdaptorApi21$ServiceBinderProxyApi21;->mServiceImpl:Landroid/support/v4/media/MediaBrowserServiceCompatApi21$ServiceImplApi21;

    new-instance v1, Landroid/support/v4/media/MediaBrowserServiceCompatApi21$ServiceCallbacksApi21;

    invoke-direct {v1, p2}, Landroid/support/v4/media/MediaBrowserServiceCompatApi21$ServiceCallbacksApi21;-><init>(Ljava/lang/Object;)V

    invoke-interface {v0, p1, v1}, Landroid/support/v4/media/MediaBrowserServiceCompatApi21$ServiceImplApi21;->addSubscription(Ljava/lang/String;Landroid/support/v4/media/MediaBrowserServiceCompatApi21$ServiceCallbacks;)V

    .line 146
    return-void
.end method

.method public connect(Ljava/lang/String;Landroid/os/Bundle;Ljava/lang/Object;)V
    .locals 2
    .param p1, "pkg"    # Ljava/lang/String;
    .param p2, "rootHints"    # Landroid/os/Bundle;
    .param p3, "callbacks"    # Ljava/lang/Object;

    .prologue
    .line 135
    iget-object v0, p0, Landroid/support/v4/media/MediaBrowserServiceCompatApi21$MediaBrowserServiceAdaptorApi21$ServiceBinderProxyApi21;->mServiceImpl:Landroid/support/v4/media/MediaBrowserServiceCompatApi21$ServiceImplApi21;

    new-instance v1, Landroid/support/v4/media/MediaBrowserServiceCompatApi21$ServiceCallbacksApi21;

    invoke-direct {v1, p3}, Landroid/support/v4/media/MediaBrowserServiceCompatApi21$ServiceCallbacksApi21;-><init>(Ljava/lang/Object;)V

    invoke-interface {v0, p1, p2, v1}, Landroid/support/v4/media/MediaBrowserServiceCompatApi21$ServiceImplApi21;->connect(Ljava/lang/String;Landroid/os/Bundle;Landroid/support/v4/media/MediaBrowserServiceCompatApi21$ServiceCallbacks;)V

    .line 136
    return-void
.end method

.method public disconnect(Ljava/lang/Object;)V
    .locals 2
    .param p1, "callbacks"    # Ljava/lang/Object;

    .prologue
    .line 140
    iget-object v0, p0, Landroid/support/v4/media/MediaBrowserServiceCompatApi21$MediaBrowserServiceAdaptorApi21$ServiceBinderProxyApi21;->mServiceImpl:Landroid/support/v4/media/MediaBrowserServiceCompatApi21$ServiceImplApi21;

    new-instance v1, Landroid/support/v4/media/MediaBrowserServiceCompatApi21$ServiceCallbacksApi21;

    invoke-direct {v1, p1}, Landroid/support/v4/media/MediaBrowserServiceCompatApi21$ServiceCallbacksApi21;-><init>(Ljava/lang/Object;)V

    invoke-interface {v0, v1}, Landroid/support/v4/media/MediaBrowserServiceCompatApi21$ServiceImplApi21;->disconnect(Landroid/support/v4/media/MediaBrowserServiceCompatApi21$ServiceCallbacks;)V

    .line 141
    return-void
.end method

.method public getMediaItem(Ljava/lang/String;Landroid/os/ResultReceiver;)V
    .locals 0
    .param p1, "mediaId"    # Ljava/lang/String;
    .param p2, "receiver"    # Landroid/os/ResultReceiver;

    .prologue
    .line 157
    return-void
.end method

.method public removeSubscription(Ljava/lang/String;Ljava/lang/Object;)V
    .locals 2
    .param p1, "id"    # Ljava/lang/String;
    .param p2, "callbacks"    # Ljava/lang/Object;

    .prologue
    .line 151
    iget-object v0, p0, Landroid/support/v4/media/MediaBrowserServiceCompatApi21$MediaBrowserServiceAdaptorApi21$ServiceBinderProxyApi21;->mServiceImpl:Landroid/support/v4/media/MediaBrowserServiceCompatApi21$ServiceImplApi21;

    new-instance v1, Landroid/support/v4/media/MediaBrowserServiceCompatApi21$ServiceCallbacksApi21;

    invoke-direct {v1, p2}, Landroid/support/v4/media/MediaBrowserServiceCompatApi21$ServiceCallbacksApi21;-><init>(Ljava/lang/Object;)V

    invoke-interface {v0, p1, v1}, Landroid/support/v4/media/MediaBrowserServiceCompatApi21$ServiceImplApi21;->removeSubscription(Ljava/lang/String;Landroid/support/v4/media/MediaBrowserServiceCompatApi21$ServiceCallbacks;)V

    .line 152
    return-void
.end method
