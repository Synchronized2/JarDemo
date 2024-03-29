package cn.face.sdk;



public class FaceInfo {
	public FaceInfo()
	{

	}

	public String faceName;

	public int detected; // 0：跟踪到的人脸; 1: 检测到的人脸; 2:检测到但不会被进行后续计算(关键点)的人脸
	                     // 注： 跟踪到的仅ID和人脸框数据有效

	public int trackId;  // 人脸ID（ID<0表示没有进入跟踪）
	
	// face rect人脸框
	public int x;        // 左上角x坐标
	public int y;        // 坐上角y坐标
	public int width;    // 人脸宽
	public int height;   // 人脸高

	
	// face_point关键点，最多68个关键点，目前使用9点关键点模型
	public float[] keypt_x;      // 关键点x坐标
	public float[] keypt_y;      // 关键点y坐标
	public float keyptScore;     // 关键点得分
	
	// face_aligned人脸对齐数据，用于提特征
	public byte[] alignedData;  // 图像数据，空间分配128*128
	public int alignedW;        // 宽
	public int alignedH;        // 高
	public int nChannels;       // 图像通道
	
	// face_quality人脸质量分
	public int   errcode;		// 质量分析错误码
	public float[] scores;      // 质量分分数项，具体含义（根据数据下标顺序）:
	  /* 0 - 人脸质量总分，0.65-1.0
	   * 1 - 清晰度，越大表示越清晰，推荐范围0.65-1.0（在启用第16项mog分数的总分时，此分数为常数1.0，请忽略）
	   * 2 - 亮度，越大表示越亮，推荐范围0.2-0.8
	   * 3 - 人脸角度，左转为正，右转为负
	   * 4 - 人脸角度，抬头为正，低头为负
	   * 5 - 人脸角度，顺时针为正，逆时针为负
	   * 6 - 左右转程度，越大表示角度越正，推荐范围0.5-1.0
	   * 7 - 抬低头程度，越大表示角度越正,推荐范围0.5-1.0
	   * 8 - 肤色接近真人肤色程度，越大表示越真实，推荐范围0.5-1.0
	   * 9 - 张嘴分数， 越大表示越可能张嘴，推荐范围0.0-0.5
	   * 10 - 左眼睁眼分数， 越大表示左眼越可能是睁眼，推荐范围0.5-1.0
	   * 11 - 右眼睁眼分数， 越大表示右眼越可能是睁眼，推荐范围0.5-1.0
	   * 12 - 戴黑框眼镜置信度，越大表示戴黑框眼镜的可能性越大，推荐范围0.0-0.5
	   * 13 - 戴墨镜的置信分，越大表示戴墨镜的可能性越大，推荐范围0.0-0.5
	   * 14 - 左眼眼睛被遮挡的置信度，越大表示眼睛越可能被遮挡，目前只在睁眼分小于0.5时有意义，推荐范围0.0-0.5
	   * 15 - 右眼眼睛被遮挡的置信度，越大表示眼睛越可能被遮挡，目前只在睁眼分小于0.5时有意义，推荐范围0.0-0.5
	   * 16 - mog清晰度，返回0.0~1.0的分数，越大越清晰，阈值建议0.100001
	   * 17~31 - 备用
	   */
}
