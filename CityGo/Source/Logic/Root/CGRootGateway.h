//
//  CGRootGateway.h
//  CityGo
//
//  Created by ruslan on 1/25/15.
//  Copyright (c) 2015 Ruslan Moskalenko. All rights reserved.
//

#import "CGSignInProtocol.h"
#import "CGSignUpProtocol.h"

@interface CGRootGateway : NSObject <CGSignInProtocol, CGSignUpProtocol>

@end
