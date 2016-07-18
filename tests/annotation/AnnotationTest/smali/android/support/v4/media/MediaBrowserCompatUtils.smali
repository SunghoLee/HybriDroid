.class public Landroid/support/v4/media/MediaBrowserCompatUtils;
.super Ljava/lang/Object;
.source "MediaBrowserCompatUtils.java"


# direct methods
.method public constructor <init>()V
    .locals 0

    .prologue
    .line 26
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public static applyOptions(Ljava/util/List;Landroid/os/Bundle;)Ljava/util/List;
    .locals 7
    .param p1, "options"    # Landroid/os/Bundle;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/util/List",
            "<",
            "Landroid/support/v4/media/MediaBrowserCompat$MediaItem;",
            ">;",
            "Landroid/os/Bundle;",
            ")",
            "Ljava/util/List",
            "<",
            "Landroid/support/v4/media/MediaBrowserCompat$MediaItem;",
            ">;"
        }
    .end annotation

    .prologue
    .local p0, "list":Ljava/util/List;, "Ljava/util/List<Landroid/support/v4/media/MediaBrowserCompat$MediaItem;>;"
    const/4 v6, 0x1

    const/4 v5, -0x1

    .line 79
    const-string v4, "android.media.browse.extra.PAGE"

    invoke-virtual {p1, v4, v5}, Landroid/os/Bundle;->getInt(Ljava/lang/String;I)I

    move-result v1

    .line 80
    .local v1, "page":I
    const-string v4, "android.media.browse.extra.PAGE_SIZE"

    invoke-virtual {p1, v4, v5}, Landroid/os/Bundle;->getInt(Ljava/lang/String;I)I

    move-result v2

    .line 81
    .local v2, "pageSize":I
    if-ne v1, v5, :cond_0

    if-ne v2, v5, :cond_0

    .line 92
    .end local p0    # "list":Ljava/util/List;, "Ljava/util/List<Landroid/support/v4/media/MediaBrowserCompat$MediaItem;>;"
    :goto_0
    return-object p0

    .line 84
    .restart local p0    # "list":Ljava/util/List;, "Ljava/util/List<Landroid/support/v4/media/MediaBrowserCompat$MediaItem;>;"
    :cond_0
    add-int/lit8 v4, v1, -0x1

    mul-int v0, v2, v4

    .line 85
    .local v0, "fromIndex":I
    add-int v3, v0, v2

    .line 86
    .local v3, "toIndex":I
    if-lt v1, v6, :cond_1

    if-lt v2, v6, :cond_1

    invoke-interface {p0}, Ljava/util/List;->size()I

    move-result v4

    if-lt v0, v4, :cond_2

    .line 87
    :cond_1
    const/4 p0, 0x0

    goto :goto_0

    .line 89
    :cond_2
    invoke-interface {p0}, Ljava/util/List;->size()I

    move-result v4

    if-le v3, v4, :cond_3

    .line 90
    invoke-interface {p0}, Ljava/util/List;->size()I

    move-result v3

    .line 92
    :cond_3
    invoke-interface {p0, v0, v3}, Ljava/util/List;->subList(II)Ljava/util/List;

    move-result-object p0

    goto :goto_0
.end method

.method public static areSameOptions(Landroid/os/Bundle;Landroid/os/Bundle;)Z
    .locals 5
    .param p0, "options1"    # Landroid/os/Bundle;
    .param p1, "options2"    # Landroid/os/Bundle;

    .prologue
    const/4 v1, 0x0

    const/4 v0, 0x1

    const/4 v4, -0x1

    .line 28
    if-ne p0, p1, :cond_1

    .line 37
    :cond_0
    :goto_0
    return v0

    .line 30
    :cond_1
    if-nez p0, :cond_3

    .line 31
    const-string v2, "android.media.browse.extra.PAGE"

    invoke-virtual {p1, v2, v4}, Landroid/os/Bundle;->getInt(Ljava/lang/String;I)I

    move-result v2

    if-ne v2, v4, :cond_2

    const-string v2, "android.media.browse.extra.PAGE_SIZE"

    invoke-virtual {p1, v2, v4}, Landroid/os/Bundle;->getInt(Ljava/lang/String;I)I

    move-result v2

    if-eq v2, v4, :cond_0

    :cond_2
    move v0, v1

    goto :goto_0

    .line 33
    :cond_3
    if-nez p1, :cond_5

    .line 34
    const-string v2, "android.media.browse.extra.PAGE"

    invoke-virtual {p0, v2, v4}, Landroid/os/Bundle;->getInt(Ljava/lang/String;I)I

    move-result v2

    if-ne v2, v4, :cond_4

    const-string v2, "android.media.browse.extra.PAGE_SIZE"

    invoke-virtual {p0, v2, v4}, Landroid/os/Bundle;->getInt(Ljava/lang/String;I)I

    move-result v2

    if-eq v2, v4, :cond_0

    :cond_4
    move v0, v1

    goto :goto_0

    .line 37
    :cond_5
    const-string v2, "android.media.browse.extra.PAGE"

    invoke-virtual {p0, v2, v4}, Landroid/os/Bundle;->getInt(Ljava/lang/String;I)I

    move-result v2

    const-string v3, "android.media.browse.extra.PAGE"

    invoke-virtual {p1, v3, v4}, Landroid/os/Bundle;->getInt(Ljava/lang/String;I)I

    move-result v3

    if-ne v2, v3, :cond_6

    const-string v2, "android.media.browse.extra.PAGE_SIZE"

    invoke-virtual {p0, v2, v4}, Landroid/os/Bundle;->getInt(Ljava/lang/String;I)I

    move-result v2

    const-string v3, "android.media.browse.extra.PAGE_SIZE"

    invoke-virtual {p1, v3, v4}, Landroid/os/Bundle;->getInt(Ljava/lang/String;I)I

    move-result v3

    if-eq v2, v3, :cond_0

    :cond_6
    move v0, v1

    goto :goto_0
.end method

.method public static hasDuplicatedItems(Landroid/os/Bundle;Landroid/os/Bundle;)Z
    .locals 11
    .param p0, "options1"    # Landroid/os/Bundle;
    .param p1, "options2"    # Landroid/os/Bundle;

    .prologue
    const/4 v8, 0x1

    const/4 v9, -0x1

    .line 45
    if-nez p0, :cond_3

    move v2, v9

    .line 46
    .local v2, "page1":I
    :goto_0
    if-nez p1, :cond_4

    move v3, v9

    .line 47
    .local v3, "page2":I
    :goto_1
    if-nez p0, :cond_5

    move v4, v9

    .line 49
    .local v4, "pageSize1":I
    :goto_2
    if-nez p1, :cond_6

    move v5, v9

    .line 53
    .local v5, "pageSize2":I
    :goto_3
    if-eq v2, v9, :cond_0

    if-ne v4, v9, :cond_7

    .line 54
    :cond_0
    const/4 v6, 0x0

    .line 55
    .local v6, "startIndex1":I
    const v0, 0x7fffffff

    .line 61
    .local v0, "endIndex1":I
    :goto_4
    if-eq v3, v9, :cond_1

    if-ne v5, v9, :cond_8

    .line 62
    :cond_1
    const/4 v7, 0x0

    .line 63
    .local v7, "startIndex2":I
    const v1, 0x7fffffff

    .line 69
    .local v1, "endIndex2":I
    :goto_5
    if-gt v6, v7, :cond_9

    if-gt v7, v0, :cond_9

    .line 74
    :cond_2
    :goto_6
    return v8

    .line 45
    .end local v0    # "endIndex1":I
    .end local v1    # "endIndex2":I
    .end local v2    # "page1":I
    .end local v3    # "page2":I
    .end local v4    # "pageSize1":I
    .end local v5    # "pageSize2":I
    .end local v6    # "startIndex1":I
    .end local v7    # "startIndex2":I
    :cond_3
    const-string v10, "android.media.browse.extra.PAGE"

    invoke-virtual {p0, v10, v9}, Landroid/os/Bundle;->getInt(Ljava/lang/String;I)I

    move-result v2

    goto :goto_0

    .line 46
    .restart local v2    # "page1":I
    :cond_4
    const-string v10, "android.media.browse.extra.PAGE"

    invoke-virtual {p1, v10, v9}, Landroid/os/Bundle;->getInt(Ljava/lang/String;I)I

    move-result v3

    goto :goto_1

    .line 47
    .restart local v3    # "page2":I
    :cond_5
    const-string v10, "android.media.browse.extra.PAGE_SIZE"

    invoke-virtual {p0, v10, v9}, Landroid/os/Bundle;->getInt(Ljava/lang/String;I)I

    move-result v4

    goto :goto_2

    .line 49
    .restart local v4    # "pageSize1":I
    :cond_6
    const-string v10, "android.media.browse.extra.PAGE_SIZE"

    invoke-virtual {p1, v10, v9}, Landroid/os/Bundle;->getInt(Ljava/lang/String;I)I

    move-result v5

    goto :goto_3

    .line 57
    .restart local v5    # "pageSize2":I
    :cond_7
    add-int/lit8 v10, v2, -0x1

    mul-int v6, v4, v10

    .line 58
    .restart local v6    # "startIndex1":I
    add-int v10, v6, v4

    add-int/lit8 v0, v10, -0x1

    .restart local v0    # "endIndex1":I
    goto :goto_4

    .line 65
    :cond_8
    add-int/lit8 v9, v3, -0x1

    mul-int v7, v5, v9

    .line 66
    .restart local v7    # "startIndex2":I
    add-int v9, v7, v5

    add-int/lit8 v1, v9, -0x1

    .restart local v1    # "endIndex2":I
    goto :goto_5

    .line 71
    :cond_9
    if-gt v6, v1, :cond_a

    if-le v1, v0, :cond_2

    .line 74
    :cond_a
    const/4 v8, 0x0

    goto :goto_6
.end method
