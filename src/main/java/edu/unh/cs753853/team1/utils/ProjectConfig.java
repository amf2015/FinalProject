package edu.unh.cs753853.team1.utils;

public class ProjectConfig {
	public static String TEAM_NAME = "Team 1";
	public static String INDEX_DIRECTORY = "index";
	public static String OUTPUT_DIRECTORY = "output";
	public static String STACK_DIRECTORY = "stackoverflow";

	// Method
	public static String METHOD_BNN_BNN = "bnn_bnn";
	public static String METHOD_LNC_LTN = "lnc_ltn";
	public static String METHOD_BL = "LM_BL";
	public static String METHOD_ANC_APC = "Anc_Apc";

	public ProjectConfig() {

	}

	public static String getTEAM_NAME() {
		return TEAM_NAME;
	}

	public static void setTEAM_NAME(String tEAM_NAME) {
		TEAM_NAME = tEAM_NAME;
	}

	public static String getINDEX_DIRECTORY() {
		return INDEX_DIRECTORY;
	}

	public static void setINDEX_DIRECTORY(String iNDEX_DIRECTORY) {
		INDEX_DIRECTORY = iNDEX_DIRECTORY;
	}

	public static String getOUTPUT_DIRECTORY() {
		return OUTPUT_DIRECTORY;
	}

	public static void setOUTPUT_DIRECTORY(String oUTPUT_DIRECTORY) {
		OUTPUT_DIRECTORY = oUTPUT_DIRECTORY;
	}

	public static String getSTACK_DIRECTORY() {
		return STACK_DIRECTORY;
	}

	public static void setSTACK_DIRECTORY(String sTACK_DIRECTORY) {
		STACK_DIRECTORY = sTACK_DIRECTORY;
	}

	public static String getMETHOD_BNN_BNN() {
		return METHOD_BNN_BNN;
	}

	public static void setMETHOD_BNN_BNN(String mETHOD_BNN_BNN) {
		METHOD_BNN_BNN = mETHOD_BNN_BNN;
	}

	public static String getMETHOD_LNC_LTN() {
		return METHOD_LNC_LTN;
	}

	public static void setMETHOD_LNC_LTN(String mETHOD_LNC_LTN) {
		METHOD_LNC_LTN = mETHOD_LNC_LTN;
	}

	public static String getMETHOD_BL() {
		return METHOD_BL;
	}

	public static void setMETHOD_BL(String mETHOD_BL) {
		METHOD_BL = mETHOD_BL;
	}

	public static String getMETHOD_ANC_APC() {
		return METHOD_ANC_APC;
	}

	public static void setMETHOD_ANC_APC(String mETHOD_ANC_APC) {
		METHOD_ANC_APC = mETHOD_ANC_APC;
	}
}
