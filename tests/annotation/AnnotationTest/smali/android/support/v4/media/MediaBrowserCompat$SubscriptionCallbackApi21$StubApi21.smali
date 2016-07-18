.class Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21$StubApi21;
.super Ljava/lang/Object;
.source "MediaBrowserCompat.java"

# interfaces
.implements Landroid/support/v4/media/MediaBrowserCompatApi21$SubscriptionCallback;


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x2
    name = "StubApi21"
.end annotation


# instance fields
.field final synthetic this$0:Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;


# direct methods
.method private constructor <init>(Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;)V
    .locals 0

    .prologue
    .line 595
    iput-object p1, p0, Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21$StubApi21;->this$0:Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method synthetic constructor <init>(Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;Landroid/support/v4/media/MediaBrowserCompat$1;)V
    .locals 0
    .param p1, "x0"    # Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;
    .param p2, "x1"    # Landroid/support/v4/media/MediaBrowserCompat$1;

    .prologue
    .line 595
    invoke-direct {p0, p1}, Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21$StubApi21;-><init>(Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;)V

    return-void
.end method


# virtual methods
.method public onChildrenLoaded(Ljava/lang/String;Ljava/util/List;)V
    .locals 6
    .param p1, "parentId"    # Ljava/lang/String;
        .annotation build Landroid/support/annotation/NonNull;
        .end annotation
    .end param
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/lang/String;",
            "Ljava/util/List",
            "<",
            "Landroid/os/Parcel;",
            ">;)V"
        }
    .end annotation

    .prologue
    .line 598
    .local p2, "children":Ljava/util/List;, "Ljava/util/List<Landroid/os/Parcel;>;"
    const/4 v1, 0x0

    .line 599
    .local v1, "mediaItems":Ljava/util/List;, "Ljava/util/List<Landroid/support/v4/media/MediaBrowserCompat$MediaItem;>;"
    if-eqz p2, :cond_0

    .line 600
    new-instance v1, Ljava/util/ArrayList;

    .end local v1    # "mediaItems":Ljava/util/List;, "Ljava/util/List<Landroid/support/v4/media/MediaBrowserCompat$MediaItem;>;"
    invoke-direct {v1}, Ljava/util/ArrayList;-><init>()V

    .line 601
    .restart local v1    # "mediaItems":Ljava/util/List;, "Ljava/util/List<Landroid/support/v4/media/MediaBrowserCompat$MediaItem;>;"
    invoke-interface {p2}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    move-result-object v0

    .local v0, "i$":Ljava/util/Iterator;
    :goto_0
    invoke-interface {v0}, Ljava/util/Iterator;->hasNext()Z

    move-result v3

    if-eqz v3, :cond_0

    invoke-interface {v0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v2

    check-cast v2, Landroid/os/Parcel;

    .line 602
    .local v2, "parcel":Landroid/os/Parcel;
    const/4 v3, 0x0

    invoke-virtual {v2, v3}, Landroid/os/Parcel;->setDataPosition(I)V

    .line 603
    sget-object v3, Landroid/support/v4/media/MediaBrowserCompat$MediaItem;->CREATOR:Landroid/os/Parcelable$Creator;

    invoke-interface {v3, v2}, Landroid/os/Parcelable$Creator;->createFromParcel(Landroid/os/Parcel;)Ljava/lang/Object;

    move-result-object v3

    invoke-interface {v1, v3}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 605
    invoke-virtual {v2}, Landroid/os/Parcel;->recycle()V

    goto :goto_0

    .line 608
    .end local v0    # "i$":Ljava/util/Iterator;
    .end local v2    # "parcel":Landroid/os/Parcel;
    :cond_0
    iget-object v3, p0, Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21$StubApi21;->this$0:Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;

    # getter for: Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;->mOptions:Landroid/os/Bundle;
    invoke-static {v3}, Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;->access$400(Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;)Landroid/os/Bundle;

    move-result-object v3

    if-eqz v3, :cond_1

    .line 609
    iget-object v3, p0, Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21$StubApi21;->this$0:Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;

    iget-object v4, p0, Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21$StubApi21;->this$0:Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;

    # getter for: Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;->mOptions:Landroid/os/Bundle;
    invoke-static {v4}, Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;->access$400(Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;)Landroid/os/Bundle;

    move-result-object v4

    invoke-static {v1, v4}, Landroid/support/v4/media/MediaBrowserCompatUtils;->applyOptions(Ljava/util/List;Landroid/os/Bundle;)Ljava/util/List;

    move-result-object v4

    iget-object v5, p0, Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21$StubApi21;->this$0:Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;

    # getter for: Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;->mOptions:Landroid/os/Bundle;
    invoke-static {v5}, Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;->access$400(Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;)Landroid/os/Bundle;

    move-result-object v5

    invoke-virtual {v3, p1, v4, v5}, Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;->onChildrenLoaded(Ljava/lang/String;Ljava/util/List;Landroid/os/Bundle;)V

    .line 615
    :goto_1
    return-void

    .line 613
    :cond_1
    iget-object v3, p0, Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21$StubApi21;->this$0:Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;

    invoke-virtual {v3, p1, v1}, Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;->onChildrenLoaded(Ljava/lang/String;Ljava/util/List;)V

    goto :goto_1
.end method

.method public onError(Ljava/lang/String;)V
    .locals 2
    .param p1, "parentId"    # Ljava/lang/String;
        .annotation build Landroid/support/annotation/NonNull;
        .end annotation
    .end param

    .prologue
    .line 619
    iget-object v0, p0, Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21$StubApi21;->this$0:Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;

    # getter for: Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;->mOptions:Landroid/os/Bundle;
    invoke-static {v0}, Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;->access$400(Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;)Landroid/os/Bundle;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 620
    iget-object v0, p0, Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21$StubApi21;->this$0:Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;

    iget-object v1, p0, Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21$StubApi21;->this$0:Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;

    # getter for: Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;->mOptions:Landroid/os/Bundle;
    invoke-static {v1}, Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;->access$400(Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;)Landroid/os/Bundle;

    move-result-object v1

    invoke-virtual {v0, p1, v1}, Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;->onError(Ljava/lang/String;Landroid/os/Bundle;)V

    .line 624
    :goto_0
    return-void

    .line 622
    :cond_0
    iget-object v0, p0, Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21$StubApi21;->this$0:Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;

    invoke-virtual {v0, p1}, Landroid/support/v4/media/MediaBrowserCompat$SubscriptionCallbackApi21;->onError(Ljava/lang/String;)V

    goto :goto_0
.end method
