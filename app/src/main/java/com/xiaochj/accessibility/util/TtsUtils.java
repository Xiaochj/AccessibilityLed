package com.xiaochj.accessibility.util;

import android.content.Context;
import android.os.Bundle;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

/**
 *
 * tts工具
 * Created by xiaochj on 2017/3/1.
 */

public class TtsUtils {

    private static TtsUtils INSTANCE = null;
    private SpeechSynthesizer mTts = null;
    private SynthesizerListener mSynListener = null;

    public TtsUtils(Context context){
        initTts(context);
    }

    public static TtsUtils getInstance(Context context){
        if(INSTANCE == null){
            synchronized (TtsUtils.class){
                if(INSTANCE == null){
                    INSTANCE = new TtsUtils(context);
                }
            }
        }
        return INSTANCE;
    }

    public void initTts(Context context){
        //1.创建SpeechSynthesizer对象, 第二个参数：本地合成时传InitListener
        mTts= SpeechSynthesizer.createSynthesizer(context, null);
        //2.合成参数设置
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");//设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "50");//设置语速
        mTts.setParameter(SpeechConstant.VOLUME, "80");//设置音量，范围0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
        //设置合成音频保存位置（可自定义保存位置），保存在“./sdcard/iflytek.pcm”
        //保存在SD卡需要在AndroidManifest.xml添加写SD卡权限
        //如果不需要保存合成音频，注释该行代码
//        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, "./sdcard/iflytek.pcm");
        //合成监听器
        mSynListener = new SynthesizerListener() {
            //会话结束回调接口，没有错误时，error为null
            public void onCompleted(SpeechError error) {
            }

            //缓冲进度回调
            //percent为缓冲进度0~100，beginPos为缓冲音频在文本中开始位置，endPos表示缓冲音频在文本中结束位置，info为附加信息。
            public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
            }

            //开始播放
            public void onSpeakBegin() {
            }

            //暂停播放
            public void onSpeakPaused() {
            }

            //播放进度回调
            //percent为播放进度0~100,beginPos为播放音频在文本中开始位置，endPos表示播放音频在文本中结束位置.
            public void onSpeakProgress(int percent, int beginPos, int endPos) {
            }

            //恢复播放回调接口
            public void onSpeakResumed() {
            }

            //会话事件回调接口
            public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
            }
        };

    }

    /**
     * 开始合成语音
     * 注：支付宝自带语音读取功能，此方法只微信调用
     */
    public void ttsSales(String str){
        mTts.startSpeaking(str, mSynListener);
    }
}
