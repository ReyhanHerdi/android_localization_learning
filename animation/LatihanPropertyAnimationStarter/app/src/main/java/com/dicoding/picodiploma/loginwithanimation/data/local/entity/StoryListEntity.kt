package com.dicoding.picodiploma.loginwithanimation.data.local.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


@Entity(tableName = "story")
@Parcelize
data class StoryListEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: String = "",

    @ColumnInfo(name = "name")
    var name: String = "",

    @ColumnInfo(name = "description")
    var description: String = "",

    @ColumnInfo(name = "photoUrl")
    var photoUrl: String = "",

    @ColumnInfo(name = "createdAt")
    var createdAt: String = ""
) : Parcelable