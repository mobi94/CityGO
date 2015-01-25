//
//  CGFacebookGateway.h
//  CityGo
//
//  Created by ruslan on 1/25/15.
//  Copyright (c) 2015 Ruslan Moskalenko. All rights reserved.
//

typedef void (^FBLoginSuccessBlock)(NSMutableDictionary *fbUser);
typedef void (^FBLoginFailureBlock)(NSError *error);

@interface CGFacebookGateway : NSObject

- (void)loginWithSuccess:(FBLoginSuccessBlock)successBlock failure:(FBLoginFailureBlock)failureBlock;

@end
