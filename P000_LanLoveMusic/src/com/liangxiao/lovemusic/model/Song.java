package com.liangxiao.lovemusic.model;

public class Song {
	private String mSongName;// �������֣���ͯ��
	private String mSongFileName;// �����ļ����֣���ͯ��.mp3
	private int mNameLength;// �������ֵĳ��ȣ���2

	public char[] getNameCharacters() {
		return mSongName.toCharArray();// �ѵ�ǰ�ĸ�������ת�����ַ�������벿��
	}

	public String getSongName() {
		return mSongName;
	}

	public void setSongName(String songName) {
		this.mSongName = songName;
		this.mNameLength = songName.length();// �������ֵĳ��ȣ���2
	}

	public String getSongFileName() {
		return mSongFileName;
	}

	public void setSongFileName(String songFileName) {
		this.mSongFileName = songFileName;
	}

	public int getNameLength() {
		return mNameLength;
	}

	// public void setNameLength(int mNameLength) {
	// this.mNameLength = mNameLength;
	// }

}
