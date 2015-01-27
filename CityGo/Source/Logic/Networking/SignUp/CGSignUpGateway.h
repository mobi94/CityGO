//
//  CGSignUpGateway.h
//  CityGo
//
//  Created by ruslan on 1/27/15.
//  Copyright (c) 2015 Ruslan Moskalenko. All rights reserved.
//

typedef void (^SignUpSuccessBlock)(BOOL success);
typedef void (^SignUpFailureBlock)(NSError *error);

@interface CGSignUpGateway : NSObject

- (void)signInUp:(NSDictionary *)userInfo WithSuccess:(SignUpSuccessBlock)successBlock
                         failure:(SignUpFailureBlock)failureBlock;

@end
