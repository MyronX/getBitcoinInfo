import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sun.tools.javac.util.Pair;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

@Slf4j
public class Request implements Runnable{


    private static final String SUMMARY = "https://chain.api.btc.com/v3/tx/unconfirmed/summary";

    private static final String BLOCK = "https://chain.api.btc.com/v3/block/latest";

    /**
     * 区块的高度
     */
    private static long globalHeight = 0;

    /**
     * 上次查询的交易池储量
     */
    private static long lastTxCounts = 0;

    /**
     * 当前这一轮交易池的最大值
     */
    private static long currMaxTxCounts = 0;

    /**
     * 根据传入的url获取不同的数据
     * @param str
     */
    public  String getReqeuest(String str)  {
        String result = "";
       try{

           URL url = new URL(str);
           URLConnection conn = url.openConnection();
           conn.setRequestProperty("accept", "*/*");
           conn.setRequestProperty("connection", "Keep-Alive");
           conn.setRequestProperty("user-agent", "");
           conn.connect();

           BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
           String line;
           while ((line = in.readLine()) != null) {
               result += "\n" + line;
           }
       } catch (Exception e) {
           log.error("getReqeuest happen Exception ",e);
           e.printStackTrace();
       }
        return result;
    }

    public  Pair<String , String> parseJson(String str) {
        Pair<String, String> pair = null;

        JSONObject jsonObject = JSON.parseObject(str);
        JSONObject dataObject = jsonObject.getJSONObject("data");
        if (dataObject.size() <=2) {
            //小于等于2，说明这是一个tx的交易
            String size = dataObject.getString("size");
            String count = dataObject.getString("count");
            pair = Pair.of(size, count);
            //logger.info("解析Summary，查询的交易池的交易数量为【{}】", count);
        }else {
            String height = dataObject.getString("height");
            String txCount = dataObject.getString("tx_count");
            pair = Pair.of(height,txCount);
            //logger.info("解析区块，区块高度为【{}】,该区块包含的交易数为【{}】",height,txCount);
        }
        return pair;
    }



    public void getDate()  {
        Pair<String , String> pairBlock;
        Pair<String , String> pairSum;
        //获取到全网的一些数据

        String blockString = getReqeuest(BLOCK);
        String sumString = getReqeuest(SUMMARY);
        if ("".equals(blockString)  || "".equals(sumString)) {
            log.debug("获取不到信息，block【{}】,summary[{}]",blockString,sumString);
        }else {
            pairBlock = parseJson(blockString);
            pairSum = parseJson(sumString);
            //区块的高度
            long blockHeight = Long.parseLong(pairBlock.fst);
            //当前区块包含的交易数
            long blockTxCounts = Long.parseLong(pairBlock.snd);
            //目前全网交易池的交易数量
            long currTxCounts = Long.parseLong(pairSum.snd);

            //区块高度增长，有新的块
            if (blockHeight > globalHeight){
                //更新区块高度
                globalHeight = blockHeight;
                //这一个epoch新增加的交易数 当前交易池数量减去上一次交易池的数量 + 区块打包的数量
                long newAddTxCounts = currMaxTxCounts - lastTxCounts + blockTxCounts;

                //更新数据
                lastTxCounts = currTxCounts;
                log.info("区块高度为[{}]，该epoch发生的交易数为[{}]",blockHeight,newAddTxCounts);
                String str = "区块高度为[{"+ blockHeight +"}]，该epoch发生的交易数为[{" + newAddTxCounts + "}]";

            }else {
                log.debug("查询交易池，当前交易池的交易总数为【{}】", currTxCounts);
                if (currTxCounts > currMaxTxCounts) {
                    currMaxTxCounts = currTxCounts;
                }
            }
        }



    }


    @Override
    public void run() {
        getDate();
    }
}
