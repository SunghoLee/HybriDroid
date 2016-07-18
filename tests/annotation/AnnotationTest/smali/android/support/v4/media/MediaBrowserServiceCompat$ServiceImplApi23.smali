.class Landroid/support/v4/media/MediaBrowserServiceCompat$ServiceImplApi23;
.super Landroid/support/v4/media/MediaBrowserServiceCompat$ServiceImplApi21;
.source "MediaBrowserServiceCompat.java"

# interfaces
.implements Landroid/support/v4/media/MediaBrowserServiceCompatApi23$ServiceImplApi23;


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Landroid/support/v4/media/MediaBrowserServiceCompat;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x2
    name = "ServiceImplApi23"
.end annotation


# instance fields
.field final synthetic this$0:Landroid/support/v4/media/MediaBrowserServiceCompat;


# direct methods
.method private constructor <init>(Landroid/support/v4/media/MediaBrowserServiceCompat;)V
    .locals 0

    .prologue
    .line 463
    iput-object p1, p0, Landroid/support/v4/media/MediaBrowserServiceCompat$ServiceImplApi23;->this$0:Landroid/support/v4/media/MediaBrowserServiceCompat;

    invoke-direct {p0, p1}, Landroid/support/v4/media/MediaBrowserServiceCompat$ServiceImplApi21;-><init>(Landroid/support/v4/media/MediaBrowserServiceCompat;)V

    return-void
.end method

.method synthetic constructor <init>(Landroid/support/v4/media/MediaBrowserServiceCompat;Landroid/support/v4/media/MediaBrowserServiceCompat$1;)V
    .locals 0
    .param p1, "x0"    # Landroid/support/v4/media/MediaBrowserServiceCompat;
    .param p2, "x1"    # Landroid/support/v4/media/MediaBrowserServiceCompat$1;

    .prologue
    .line 463
    invoke-direct {p0, p1}, Landroid/support/v4/media/MediaBrowserServiceCompat$ServiceImplApi23;-><init>(Landroid/support/v4/media/MediaBrowserServiceCompat;)V

    return-void
.end method


# virtual methods
.method public getMediaItem(Ljava/lang/String;Landroid/support/v4/media/MediaBrowserServiceCompatApi23$ItemCallback;)V
    .locals 2
    .param p1, "mediaId"    # Ljava/lang/String;
    .param p2, "cb"    # Landroid/support/v4/media/MediaBrowserServiceCompatApi23$ItemCallback;

    .prologue
    .line 468
    new-instance v0, Landroid/support/v4/media/MediaBrowserServiceCompat$ServiceImplApi23$1;

    iget-object v1, p0, Landroid/support/v4/media/MediaBrowserServiceCompat$ServiceImplApi23;->this$0:Landroid/support/v4/media/MediaBrowserServiceCompat;

    # getter for: Landroid/support/v4/media/MediaBrowserServiceCompat;->mHandler:Landroid/support/v4/media/MediaBrowserServiceCompat$ServiceHandler;
    invoke-static {v1}, Landroid/support/v4/media/MediaBrowserServiceCompat;->access$100(Landroid/support/v4/media/MediaBrowserServiceCompat;)Landroid/support/v4/media/MediaBrowserServiceCompat$ServiceHandler;

    move-result-object v1

    invoke-direct {v0, p0, v1, p2}, Landroid/support/v4/media/MediaBrowserServiceCompat$ServiceImplApi23$1;-><init>(Landroid/support/v4/media/MediaBrowserServiceCompat$ServiceImplApi23;Landroid/os/Handler;Landroid/support/v4/media/MediaBrowserServiceCompatApi23$ItemCallback;)V

    .line 480
    .local v0, "receiverCompat":Landroid/support/v4/os/ResultReceiver;
    iget-object v1, p0, Landroid/support/v4/media/MediaBrowserServiceCompat$ServiceImplApi23;->mServiceImpl:Landroid/support/v4/media/MediaBrowserServiceCompat$ServiceImpl;

    invoke-virtual {v1, p1, v0}, Landroid/support/v4/media/MediaBrowserServiceCompat$ServiceImpl;->getMediaItem(Ljava/lang/String;Landroid/support/v4/os/ResultReceiver;)V

    .line 481
    return-void
.end method
