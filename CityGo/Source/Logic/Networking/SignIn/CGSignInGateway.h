//
//  CGSignInGateway.h
//  CityGo
//
//  Created by ruslan on 1/25/15.
//  Copyright (c) 2015 Ruslan Moskalenko. All rights reserved.
//

typedef void (^SignInSuccessBlock)(BOOL success);
typedef void (^SignInFailureBlock)(NSError *error);

@interface CGSignInGateway : NSObject

- (void)signInUsingFBWithSuccess:(SignInSuccessBlock)successBlock
                         failure:(SignInFailureBlock)failureBlock;

@end
