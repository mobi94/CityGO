//
//  CGSignUpProtocol.h
//  CityGo
//
//  Created by ruslan on 1/25/15.
//  Copyright (c) 2015 Ruslan Moskalenko. All rights reserved.
//

typedef void (^SignUpHandler)(NSError *error);

@protocol CGSignUpProtocol <NSObject>

- (void)signUp:(NSDictionary *)userInfo WithBlock:(SignUpHandler)completitionHandler;

@end
