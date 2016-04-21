package com.byw.stock.house.track.client.listener;

import com.byw.stock.house.track.api.HttpClientReferent;
import com.byw.stock.house.track.api.IResponseListener;
import com.byw.stock.house.track.services.MongoDBStorageService;
import com.byw.stock.house.platform.log.PlatformLogger;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.result.UpdateResult;


/**
 * 
 * 取得盘面信息.
 * 
 * 总盘面，流通盘面.
 * 
 * @title StockInfoResponseListener
 * @package com.byw.stock.house.track.client.listener
 * @author baiyanwei
 * @version
 * @date Feb 16, 2016
 * 
 */
public class StockTotalShareResponseListener implements IResponseListener {

    final private static PlatformLogger _logger = PlatformLogger.getLogger(StockTotalShareResponseListener.class);
    final private static String SLIPT_TEXT = "~";

    final private MongoDBStorageService _mss;
    final private String _code;

    public StockTotalShareResponseListener(MongoDBStorageService mss, String code) {

        this._mss = mss;
        this._code = code;
    }

    @Override
    public void fireDone(HttpClientReferent hcr, Object content) {

        if (content == null) {
            return;
        }
        String text = (String) content;
        if (text.length() == 0) {
            return;
        }
        /*
        0: 未知
        1: 名字
        2: 代码
        3: 当前价格price
        4: 昨收
        5: 今开
        6: 成交量（手）
        7: 外盘
        8: 内盘
        9: 买一
        10: 买一量（手）
        11-18: 买二 买五
        19: 卖一
        20: 卖一量
        21-28: 卖二 卖五
        29: 最近逐笔成交
        30: 时间
        31: 涨跌
        32: 涨跌%
        33: 最高
        34: 最低
        35: 价格/成交量（手）/成交额
        36: 成交量（手）
        37: 成交额（万）
        38: 换手率
        39: 市盈率
        40:  
        41: 最高
        42: 最低
        43: 振幅
        44: 流通市值 CIRCULATION MARKET VALUE
        45: 总市值 MARKET CAPITALIZATION
        46: 市净率
        47: 涨停价
        48: 跌停价
        SCOPE OF BUSINESS
        */
        String[] items = text.split(SLIPT_TEXT);
        if (items.length != 50) {
            return;
        }
        float cvs = Float.parseFloat(items[44]) / Float.parseFloat(items[3]);
        float totalShare = Float.parseFloat(items[45]) / Float.parseFloat(items[3]);

        DBObject updateCondition = new BasicDBObject();
        //where code=''
        updateCondition.put("code", _code);

        DBObject updatedValue = new BasicDBObject();
        updatedValue.put("cvs", cvs);
        updatedValue.put("totalShare", totalShare);

        DBObject updateSetValue = new BasicDBObject("$set", updatedValue);

        UpdateResult up = this._mss.getStockCollection("stock_code").updateOne(updateCondition, updateSetValue);
        _logger.info("update " + this._code + " match:" + up.getMatchedCount() + ",set:totalShare" + totalShare + ",cvs:" + cvs);
    }

    @Override
    public void fireException(HttpClientReferent hcr, Throwable cause) {

        _logger.exception(cause);

    }

}
