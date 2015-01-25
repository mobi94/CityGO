//
//  CGTwitterGateway.h
//  CityGo
//
//  Created by ruslan on 1/25/15.
//  Copyright (c) 2015 Ruslan Moskalenko. All rights reserved.
//

typedef void (^TwitterLoginSuccessBlock)(NSDictionary *twitterUser);
typedef void (^TwitterLoginFailureBlock)(NSError *error);

@interface CGTwitterGateway : NSObject

- (void)login:(UIViewController *)controller WithSuccess:(TwitterLoginSuccessBlock)successBlock failure:(TwitterLoginFailureBlock)failureBlock;

@end
