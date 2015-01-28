//
//  CGRootGateway.m
//  CityGo
//
//  Created by ruslan on 1/25/15.
//  Copyright (c) 2015 Ruslan Moskalenko. All rights reserved.
//

#import "CGRootGateway.h"
#import "CGSignInGateway.h"
#import "CGSignUpGateway.h"

@interface CGRootGateway ()

@property(strong, nonatomic) CGSignInGateway *signInGateway;
@property(strong, nonatomic) CGSignUpGateway *signUpGateway;

@end

@implementation CGRootGateway

- (instancetype)init
{
    self = [super init];
    
    if (self)
    {
        self.signInGateway = [[CGSignInGateway alloc] init];
        self.signUpGateway = [[CGSignUpGateway alloc] init];
    }
    
    return self;
}

#pragma mark -
#pragma mark Sign In Facebook

- (void)signInUsingFbWithBlock:(SignInHandler)completitionHandler
{
    [self.signInGateway signInUsingFBWithSuccess:^(BOOL success)
    {
        completitionHandler(nil);
    }
    failure:^(NSError *error)
    {
        completitionHandler(error);
    }];
}

#pragma mark -
#pragma mark Sign In Facebook

- (void)signInUsingTwitter:(UIViewController *)controller WithBlock:(SignInHandler)completitionHandler
{
    [self.signInGateway signInUsingTwitter:controller WithSuccess:^(BOOL success)
    {
        completitionHandler(nil);
    }
    failure:^(NSError *error)
    {
        completitionHandler(error);
    }];
}

#pragma mark -
#pragma mark Sign In VK

- (void)signInUsingVkWithBlock:(SignInHandler)completitionHandler
{
    [self.signInGateway signInUsingVkWithSuccess:^(BOOL success)
    {
        completitionHandler(nil);
    }
    failure:^(NSError *error)
    {
        completitionHandler(error);
    }];
}

#pragma mark -
#pragma mark Sign Up

- (void)signUp:(NSDictionary *)userInfo WithBlock:(SignUpHandler)completitionHandler
{
    [self.signUpGateway signInUp:userInfo WithSuccess:^(BOOL success)
    {
        completitionHandler(nil);
    }
    failure:^(NSError *error)
    {
        completitionHandler(error);
    }];
}

#pragma mark -
#pragma mark Sign In

- (void)signInUsingUserInfo:(NSDictionary *)userInfo WithBlock:(SignInHandler)completitionHandler
{
    [self.signInGateway signInUsingUserInfo:userInfo WithSuccess:^(BOOL success)
    {
        completitionHandler(nil);
    }
    failure:^(NSError *error)
    {
        completitionHandler(error);
    }];
}

@end
