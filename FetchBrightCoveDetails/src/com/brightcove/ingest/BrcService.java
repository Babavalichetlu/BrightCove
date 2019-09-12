package com.brightcove.ingest;


public interface BrcService {

    public String getReadToken();
    public String getWriteToken();
    public String getPreviewPlayerLoc();
	public String getPreviewPlayerListLoc();
	public String getDefVideoPlayerID();
	public String getDefVideoPlayerKey();
	public String getDefPlaylistPlayerID();
	public String getDefPlaylistPlayerKey();
}
