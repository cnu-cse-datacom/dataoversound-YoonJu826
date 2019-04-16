package com.example.sound.devicesound;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

<<<<<<< HEAD
import static java.lang.Math.abs;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.*;

=======
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.*;

import java.util.ArrayList;

>>>>>>> fe2928108c9288e087fbdf1fdb39efe5bd0b8fce
public class Listentone {

    int HANDSHAKE_START_HZ = 4096;
    int HANDSHAKE_END_HZ = 5120 + 1024;

    int START_HZ = 1024;
    int STEP_HZ = 256;
    int BITS = 4;

    int FEC_BYTES = 4;

    private int mAudioSource = MediaRecorder.AudioSource.MIC;
    private int mSampleRate = 44100;
    private int mChannelCount = AudioFormat.CHANNEL_IN_MONO;
    private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private float interval = 0.1f;

    private int mBufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannelCount, mAudioFormat);

    public AudioRecord mAudioRecord = null;
    int audioEncodig;
    boolean startFlag;
    FastFourierTransformer transform;

<<<<<<< HEAD
    public Listentone(){
=======

    public Listentone(){

>>>>>>> fe2928108c9288e087fbdf1fdb39efe5bd0b8fce
        transform = new FastFourierTransformer(DftNormalization.STANDARD);
        startFlag = false;
        mAudioRecord = new AudioRecord(mAudioSource, mSampleRate, mChannelCount, mAudioFormat, mBufferSize);
        mAudioRecord.startRecording();
<<<<<<< HEAD
    }

    private int findPowerSize(int round) {
        //2의 제곱수만 받아서 기존 블럭사이즈에 가장 가까운 제곱수 찾아줌
        int calculate = 1;

        while(true){
            calculate = calculate * 2;
            if(calculate >= round){
                return calculate;
            }
        }
    }

    private double findFrequency(double[] toTransform){//PreRequest에서 호출(dominant함수와 동일!!)
        int len = toTransform.length;
        double[] real = new double[len];
        double[] img = new double[len];
        double realNum;
        double imgNum;
        double[] mag = new double[len];

        double peak_coeff = 0;
        int j=0;

        Complex[] complx = transform.transform(toTransform, TransformType.FORWARD);//fft관련
        double[] freq = this.fftfreq(complx.length, 1); // fftfreq 함수 호출

        for(int i=0; i< complx.length; i++){
            realNum = complx[i].getReal();
          //  Log.d("real Num:", Double.toString(realNum));
            imgNum = complx[i].getImaginary();
          //  Log.d("img num:", Double.toString(imgNum));
            mag[i] = Math.sqrt((realNum * realNum)+(imgNum * imgNum));
           // Log.d("mag:", Double.toString(mag[i]));
        }

        for(int i=0; i < complx.length; i++){
            double value = mag[i];
            if(value > peak_coeff){
                peak_coeff = value;
                j = i;
            }
        }
        double peak_freq = freq[j];
        return abs(peak_freq * mSampleRate);
    }

    private double[] fftfreq(int length, int distance) {
        //참고자료 numpy함수 해석해서 작성
        double n = 1.0 / (length * distance);
        int[] temp = new int[length];
        double[] result = new double[length];

        int num1 = (length-1)/2 + 1;

        for(int i=0; i <= num1; i++){
            temp[i] = i;
        }
        int num2 = -(length / 2);
        for(int i=num1+1; i<length; i++){
            temp[i] = num2;
            num2--;
        }
        for(int i=0; i<length; i++){
            result[i] = temp[i] * n;
        }
        return result;
    }

    public boolean match(double freq1, double freq2){
        return abs(freq1 - freq2) < 20;
    }

    public List<Integer> decode_bitchunks(int chunk_bits, List<Integer> chunks){
        List<Integer> out_bytes = new ArrayList<>();
        int next_read_chunk = 0;
        int next_read_bit = 0;
        int nByte = 0;
        int bits_left = 8;

        while(next_read_chunk < chunks.size()){
            int can_fill = chunk_bits  - next_read_bit;
            int to_fill = Math.min(bits_left, can_fill);
            int offset = chunk_bits - next_read_bit - to_fill;
            nByte <<= to_fill;
            int shifted = chunks.get(next_read_chunk) & (((1 << to_fill) - 1) << offset);
            nByte |= shifted >> offset;
            bits_left -= to_fill;
            next_read_bit += to_fill;

            if (bits_left <= 0) {
                out_bytes.add(nByte);
                nByte = 0;
                bits_left = 8;
            }

            if(next_read_bit >= chunk_bits){
                next_read_chunk += 1;
                next_read_bit -= chunk_bits;
            }
        }
        return out_bytes;
    }

    public List<Integer> extract_packet(List<Double> freqs){
        List<Double> sample_freqs = new ArrayList<Double>();
        List<Integer> bit_chunks = new ArrayList<Integer>();
        List<Integer> newBit_chunks = new ArrayList<Integer>();

        for(int i=0; i< freqs.size(); i++){
            sample_freqs.add(freqs.get(i));
        }
        for(int i=0; i< sample_freqs.size(); i++){
            int f = (int)(Math.round((sample_freqs.get(i) - START_HZ) / STEP_HZ));
            bit_chunks.add(f);
        }
        for(int i=1; i<bit_chunks.size(); i++){
            if(bit_chunks.get(i) > 0 && bit_chunks.get(i) < Math.pow(2,BITS)){
                newBit_chunks.add(bit_chunks.get(i));
            }
        }
        return decode_bitchunks(BITS, newBit_chunks);
    }

    public void PreRequest() {// 처음으로 호출되는 메소드에 해당함(listen_linux)
        // recorder로부터 음성을 읽음
        int blocksize = findPowerSize((int)(long)Math.round(interval/2*mSampleRate)); // -> num_frames
        // Math.round -> 실수의 소수점 첫번째 자리 반올림해서 정수로 리턴(정수가 리턴됨)
        short[] buffer = new short[blocksize]; // buffer를 이용하여 fourier transform실행
        double[] chunk = new double[blocksize]; // np.fromstring(data, dtype=np.int16)

        List<Double> packet = new ArrayList<>();
        List<Integer> byte_stream = new ArrayList<>();
        // byte_stream : extract_packet의 return형 : List<Integer>

        while(true) {
            int bufferedReadResult = mAudioRecord.read(buffer, 0, blocksize);

            for(int i=0; i < blocksize; i++){
                chunk[i] = buffer[i];
            }

            double dom = findFrequency(chunk);

            if(startFlag && match(dom, HANDSHAKE_END_HZ)){
              //  System.out.println("dd"); -> 디버깅용
                byte_stream = extract_packet(packet);
                Log.d("byte_stream", byte_stream.toString());
                String finalResult = "";

                for(int i=0; i < byte_stream.size(); i++){
                        finalResult += Character.toString((char)(int)(byte_stream.get(i)));
                }

                Log.d("ListenTone", finalResult.toString());
                packet.clear();//arrayList 모두 비우기
                startFlag = false;
            }
            else if(startFlag)
                packet.add(dom);
            else if(match(dom, HANDSHAKE_START_HZ))
                startFlag = true;
                Log.d("Dom", ""+dom);
        }
    }
}

=======




    }




}
>>>>>>> fe2928108c9288e087fbdf1fdb39efe5bd0b8fce
