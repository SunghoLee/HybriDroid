.class public Lkr/ac/kaist/wala/hybridroid/test/annotation/JSBridge;
.super Ljava/lang/Object;
.source "JSBridge.java"


# direct methods
.method public constructor <init>()V
    .locals 0

    .prologue
    .line 5
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public getFirstName()Ljava/lang/String;
    .locals 1
    .annotation runtime Landroid/webkit/JavascriptInterface;
    .end annotation

    .prologue
    .line 9
    const-string v0, "sungho"

    return-object v0
.end method

.method public getLastName()Ljava/lang/String;
    .locals 1

    .prologue
    .line 13
    const-string v0, "lee"

    return-object v0
.end method
