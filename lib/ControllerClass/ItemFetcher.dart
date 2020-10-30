import 'package:vb_v0/ModelClass/Item.dart';
import 'package:flutter/widgets.dart';

class ItemFetcher {
    List<Item> items;
    static const FitMode = BoxFit.fill;
    ItemFetcher(){
      items = <Item> [
        Item(
          iid: "00000",
          name: "Iphone 13 pro",
          image: "assets/images/iphone13.jpg"
        ),
        Item(
          iid: "00001",
          name: "Airpod 3.0",
          image: "assets/images/airpod.jpg"        
        )
        
      ];
    }
    
}