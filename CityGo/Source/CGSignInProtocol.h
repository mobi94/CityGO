//
//  CGSignInProtocol.h
//  CityGo
//
//  Created by ruslan on 1/25/15.
//  Copyright (c) 2015 Ruslan Moskalenko. All rights reserved.
//

typedef void (^SignInHandler)(NSError *error);

@protocol CGSignInProtocol <NSObject>

- (void)signInUsingFbWithBlock:(SignInHandler)completitionHandler;

@end
