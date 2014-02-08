package com.facetoe.remotempd;

import com.google.gson.Gson;
import org.a0z.mpd.Music;
import java.util.List;

/**
 * RemoteMPD
 * Created by facetoe on 8/02/14.
 */
public class MPDCachedPlaylist {
    private String md5Hash;
    private List<Music> musicList;

    public MPDCachedPlaylist(String md5Hash, List<Music> musicList) {
        this.md5Hash = md5Hash;
        this.musicList = musicList;
    }

    public MPDCachedPlaylist(String json) {
        MPDCachedPlaylist tmp = new Gson().fromJson(json, MPDCachedPlaylist.class);
        this.md5Hash = tmp.getMd5Hash();
        this.musicList = tmp.getMusicList();
    }

    public boolean hashCodeIsEqual(String newHash) {
        return md5Hash.equals(newHash);
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public String getMd5Hash() {
        return md5Hash;
    }

    public void setMd5Hash(String md5Hash) {
        this.md5Hash = md5Hash;
    }

    public List<Music> getMusicList() {
        return musicList;
    }

    public void setMusicList(List<Music> musicList) {
        this.musicList = musicList;
    }

    @Override
    public String toString() {
        return "MPDCachedPlaylist{" +
                "md5Hash='" + md5Hash + '\'' +
                ", musicList=" + musicList +
                '}';
    }
}