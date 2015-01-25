//
//  CGVkGateway.h
//  CityGo
//
//  Created by ruslan on 1/26/15.
//  Copyright (c) 2015 Ruslan Moskalenko. All rights reserved.
//

typedef void (^VKLoginSuccessBlock)(NSDictionary *vkUser);
typedef void (^VKLoginFailureBlock)(NSError *error);

@interface CGVkGateway : NSObject

- (void)loginWithSuccess:(VKLoginSuccessBlock)successBlock failure:(VKLoginFailureBlock)failureBlock;

@end
