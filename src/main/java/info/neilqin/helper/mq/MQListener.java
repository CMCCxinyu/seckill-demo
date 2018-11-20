package info.neilqin.helper.mq;

import info.neilqin.api.IGoodsService;
import info.neilqin.api.IOrderService;
import info.neilqin.common.constants.Constants;
import info.neilqin.entity.po.OrderPO;
import info.neilqin.entity.vo.GoodsVO;
import info.neilqin.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Neil
 * @date Create in 0:48 2018/11/21
 */
@Component
@Slf4j
public class MQListener {

    @Autowired
    IGoodsService goodsService;
    @Autowired
    IOrderService orderService;

    @RabbitListener(queues = Constants.Queue.SECKILL_QUEUE)
    public void receive(SeckillMsg msg){
        GoodsVO goods = this.goodsService.getGoodsSeckillDetail(msg.getGoodsId());
        Integer stock = goods.getStockCount();
        if (stock <= 0){
            return;
        }
        OrderPO order = orderService.findSeckillOrderByUidAndGid(msg.getUser().getId(), goods.getGoodsId());
        if (null != order){
            return;
        }
        orderService.seckill(msg.getUser(),goods);
    }

}